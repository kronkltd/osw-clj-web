(ns osw-clj-web.acl-test
  (:use [osw-clj-web.acl] :reload-all)
  (:use clojure.test)
  (:import org.onesocialweb.model.acl.DefaultAclFactory))

(def *rule*)

(defn acl-fixture
  [f]
  (binding [*rule* (.aclRule (DefaultAclFactory.))]
    (f)))

(use-fixtures :each acl-fixture)

(deftest test-line
  (is (vector? (line *rule*))
      "returns a vector"))

(deftest test-section
  (is
   (vector? (section {:aclRules true})))
  (is
   (nil? (section nil))))
