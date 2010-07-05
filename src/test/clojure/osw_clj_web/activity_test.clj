(ns osw-clj-web.activity-test
  (:use [osw-clj-web.activity
         ] :reload-all)
  (:use clojure.test)
  (:require [osw-lib-clj.core :as osw])
  (:import org.onesocialweb.model.activity.DefaultActivityFactory)
  )

(deftest test-add-link
  (let [activity (.entry (DefaultActivityFactory.))
        link "http://example.com/"]
    (add-link activity link)

    (is
     (filter
      #(= % link)
      (map #(.getHref %)
           (.getLinks activity))))))

(deftest test-add-recipient
  (let [activity (.entry (DefaultActivityFactory.))
        recipient "alice@xmpp.loc"]
    (add-recipient activity recipient)

    (is
     (filter
      #(= % recipient)
      (map #(.getHref %)
           (.getRecipients activity))))))

(deftest test-construct-activity)

(deftest test-delete-post-handler)

(deftest test-create-handler)

(deftest test-form
  (is (vector? (form nil))))

(deftest test-footer-buttons
  (is (vector? (footer-buttons))))

(deftest test-links-section
  (testing "with no links"
    (is (nil? (links-section (.entry (DefaultActivityFactory.)))))))

(deftest test-published-link
  (testing "with no published info"
    (is (nil? (published-link (.entry (DefaultActivityFactory.)))))))

(deftest test-aesthetica-icon
  #_(is (string? (aesthetica-icon "foo"))))

(deftest test-replies-line)

(deftest test-replies-section
  (testing "with no replies"
    (binding [osw/replies (fn [activity] nil)]
      (is (nil? (replies-section (.entry (DefaultActivityFactory.)))))))
  (testing "with replies"
    (binding [osw/replies (fn [activity] [])]
      (let [activity (.entry (DefaultActivityFactory.))]
        (is (vector? (replies-section activity)))))))

(deftest test-to-xml
  (is (string?
       (to-xml (.entry (DefaultActivityFactory.))))))

(deftest test-line)

(deftest test-section)

(deftest test-create)

(deftest test-show)

