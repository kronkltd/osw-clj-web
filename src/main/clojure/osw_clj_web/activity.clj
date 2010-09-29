(ns osw-clj-web.activity
  (:use osw-clj-web.view
        clojure.stacktrace
        render-ring.core
        clj-time.coerce
        clj-time.format)
  (:require [osw-clj-web.acl :as acl]
            [osw-clj-web.actor :as actor]
            [osw-lib-clj.core :as osw]
            [osw-lib-clj.acl :as acl-model]
            [osw-lib-clj.activity :as activity-model]
            [osw-clj-web.recipient :as recipient]
            [osw-clj-web.object :as object]
            [hiccup.form-helpers :as f])
  (:import org.onesocialweb.model.atom.DefaultAtomReplyTo
           org.onesocialweb.model.atom.DefaultAtomLink
           org.onesocialweb.model.atom.DefaultAtomFactory
           org.onesocialweb.xml.dom.imp.DefaultActivityDomWriter
           org.dom4j.dom.DOMDocument
           org.dom4j.io.DOMReader
           org.dom4j.io.OutputFormat
           java.io.StringWriter
           org.dom4j.io.XMLWriter))

(comment
  (construct-activity
   {:content "Hello world"
    :recipients ["duck@mycyclopedia.net"
                 "test@mycyclopedia.net"]
    :links [{:href "/posts/4"
             :rel "alternate"
             :type "text/html"}
            {:href "/posts/4.atom"
             :rel "alternate"
             :type "application/xml+atom"}]
    :objects [{:type :comment
               :content "Nice Post"}]}))

(defn add-link
  [activity link]
  (let [atom-link (DefaultAtomLink.)]
    (.setHref atom-link link)
    (.addLink activity atom-link)))

(defn add-recipient
  [activity recipient]
  (if (not= recipient "")
    (let [reply-to (DefaultAtomReplyTo.)]
      (.setHref reply-to recipient)
      (.addRecipient activity reply-to))))

(defn construct-activity
  [{{content "content",
     recipients "recipients[]"
     links "links[]"
     parent-id "parent-id"
     parent-jid "parent-jid"} :params,
     :as request}]
  (let [activity (activity-model/entry content)]
    ;; ACL Rule (currently defaulting to public)
    (.addAclRule activity (acl-model/public-rule))

    ;; Recipients
    (if (and recipients (not= recipients ""))
      (doseq [recipient
              (if (vector? recipients)
                recipients [recipients])]
         (add-recipient activity recipient)))

    ;; Links
    (if (and links (not= links ""))
      (doseq [link (if (vector? links)
                     links [links])]
        (if (not (= link ""))
          (do (println "Adding Link: " link)
              (add-link activity link)))))

    (println "parent-id: " parent-id)
    (println "parent-jid: " parent-jid)
    ;; Is this a comment?
    (if (and
         parent-id
         parent-jid
         (not= parent-id "")
         (not= parent-jid ""))
      ;; Make a comment
      (do
        (.setParentId activity parent-id)
        (.setParentJID activity parent-jid)))

    ;; Objects
    (let [object (activity-model/construct-object
                  {:content content})]
      (.addObject activity object))

    ;; Verbs
    (let [verb (activity-model/verb)]
      (.addVerb activity verb))

    ;; Add the generator
    (.setGenerator
     activity
     (let [generator (.generator (DefaultAtomFactory.))]
       (doto generator
         (.setUri "http://mycyclopedia.net/")
         (.setVersion "0.1.0-SNAPSHOT")
         (.setText "osw-clj-web"))))

    ;; return the activity
    activity))

(defn delete-post-handler
  [{{id "id"} :params,
    :as request}]
  {:body
   (if (osw/delete-activity! id)
     "true" "false")})

(defn create-handler
  [request]
  (let [activity (construct-activity request)]
    (println activity)
    (println request)
    (osw/post! activity))
  {:status 302
   :headers {"Location" "/inbox"}})

(defn form
  [request]
  [:section.post-activity
   (f/form-to
    [:post "/post"]
    (if-let [route-params (:route-params request)]
      (if-let [id (route-params "id")]
        (f/hidden-field :parent-id id)))
    (if-let [params (:params request)]
      (if-let [jid (params "jid")]
        (f/hidden-field :parent-jid jid)))
    [:section (f/text-area :content)]
    [:section.recipient-section
     [:h "Recipients"]
     [:p
      (f/text-field {:class "recipient"} "recipient-input")
      [:a.add-recipient {:href "#"} "Add Recipient"]]
     [:ul.recipient-list
      [:li.recipient-line.hidden
       [:span.recipient-name]
       (f/hidden-field "recipients[]")
       [:a.remove-recipient {:href "javascript:void()"} "Remove"]]]]
    [:section.privacy-section
     [:p "Public" [:a.change-link {:href "#"} "change"]]
     [:div.hidden
      [:ul#add-rule-list.hidden
       [:li
        (f/text-field "rules[]")
        [:a.remove-rule {:href "javascript:void()"} "Remove"]]]
      (f/drop-down :privacy ["public"
                             "group"
                             "private"])
      [:a.add-rule {:href "#"} "Add Rule"]]]
    [:section.link-section
     [:div.hidden
      [:ul.links-list
       [:li.hidden
        [:span.link-label]
        (f/hidden-field "links[]")
        [:a.remove-link {:href "javascript:void()"} "Remove"]]]
      (f/text-field "links[]")]
     [:p [:a.add-link {:href "javascript:void()"} "Add Link"]]]
    [:p
     (f/submit-button "Submit")])])

(defn footer-buttons
  []
  [:ul.buttons
   [:li ]])

(defn links-section
  [activity]
  (if-let [links (seq (.getLinks activity))]
    [:section.links
     [:h "Links: " (count links)]
     (dump links)
     [:ul
      (doall
       (map
        (fn [link]
          [:li (.getHref link)
           (.getRel link)])
        links))]]))

(defn published-link
  [activity]
  (if-let [id (.getId activity)]
    (if-let [published (.getPublished activity)]
      [:time
       [:a {:href (str "/posts/" id)}
        (unparse
         (formatters :date)
         (from-date published))]])))

(declare replies-section)

(defn aesthetica-icon
  "returns a path to an icon with the given name"
  [name]
  (str
   "/public/images/aesthetica-version-2/png/24x24/"
   name
   ".png"))

(declare line)

(defn replies-section
  [activity]
  (if-let [replies (osw-lib-clj.core/replies activity)]
    [:section.comments
     (dump replies)
     [:ul
      (map
       line
       (sort-by #(.getPublished %)
                replies))]]))

(defn to-xml
  [activity]
  (let [document (DOMDocument.)
        reader (DOMReader.)
        activity-dom-writer (DefaultActivityDomWriter.)
        activity-element (.toElement activity-dom-writer
                                     activity document)
        output-format (OutputFormat/createPrettyPrint)
        output-string (StringWriter.)
        xml-writer (XMLWriter. output-string output-format)]
    (.write xml-writer (.read reader document))
    (.toString output-string)))

(defn line
  [activity]
  (try
   (let [activity-bean (bean activity)
         id (.getId activity)]
     [:article {:id id}
      [:aside.avatar
       (actor/avatar-for (.getUri (.getActor activity)))]
      [:section.activity-content
       [:header
        [:div.actor-actions
         (actor/section (.getActor activity))
         [:a.shout-button    {:href "#"}
          [:img {:src (aesthetica-icon "user_comment")
                 :alt "Shout at author"}]]]
        [:div.publish-actions
         [:a.like-activity   {:href "#"}
          [:img {:src (aesthetica-icon "favorite_add")
                 :alt "like"}]]
         [:a.edit-activity   {:href "#"}
          [:img {:src (aesthetica-icon "page_edit")
                 :alt "Edit"}]]
         [:a.delete-activity {:href "#"}
          [:img {:src (aesthetica-icon "remove")
                 :alt "Delete"}]]]
        (recipient/section activity-bean)]
       (object/section activity)
       [:p
        "posted on "
        (published-link activity)
        (if (.hasGenerator activity)
          (let [generator (.getGenerator activity)]
            (list
             " using "
             [:a {:href (.getUri generator)}
              (.getText generator)])))]]
      [:section.comments
       [:p [:a.comment {:href "#"} "Comment"]]
       (replies-section activity)
       [:div.comment-form]]
      [:footer
       (dump-unescaped (to-xml activity))
       (dump activity)]])
   (catch NullPointerException e
     [:p "An error done happened"
      (with-out-str
        (print-stack-trace e))])))

(defn section
  [activities]
  [:section.activities
   (doall (map line activities))])

(defn create
  [request])

(defn show
  [request]
  (println request)
  (let [id ((:params request) "id")]
    id))

(defpage #'show :html
  [id]
  {:body
   [:p id]})

(defpage #'create :html
  [request]
  {:body (form request)})
