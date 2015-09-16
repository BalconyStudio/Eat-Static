;;  Copyright (c) Andrew Stoeckley, 2015. All rights reserved.

;;  The use and distribution terms for this software are covered by the
;;  Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;;  which can be found in the license file at the root directory of this distribution.
;;  By using this software in any fashion, you are agreeing to be bound by
;;  the terms of this license.
;;  You must not remove this notice, or any other, from this software.

(ns eat-static.validations-test
  (:require [clojure.test :refer :all]
            [eat-static.validations :refer :all]))

;; A mild handful of tests

(deftest df-output-int
  (is (df a (:i) [b] (* b 2)))
  (is (df b (:i) [c] (int (* 2.0 c))))
  (is (thrown? AssertionError (c a :b 1.2)))
  (is (thrown? AssertionError (c a :b -123.0)))
  (is (thrown? AssertionError (a {:b 1.2})))
  (is (thrown? AssertionError (c a :b 1.2)))
  (is (thrown? AssertionError (c a :c 1.2)))
  (is (thrown? AssertionError (c a :c 1)))
  (is (= 10 (c a :b 5)))
  (is (= 10 (c a :b (int 5.9))))
  (is (= 20 (a {:b 10})))
  (is (= 40 (c b :c 20.1))))

(deftest df-output-keys
  (is (df a ((:b)) [c] c))
  (is (= {:b 5} (c a :c {:b 5})))
  (is (thrown? AssertionError (c a :c 5)))
  (is (thrown? AssertionError (c a :c {:c 1})))
  (is (df a ((:b) (:c)) [c] c))
  (is (= {:b 5 :c :hi} (c a :c {:b 5 :c :hi})))
  (is (thrown? AssertionError (c a :c 5)))
  (is (thrown? AssertionError (c a :c {:c 1})))
  (is (thrown? AssertionError (c a :b 5)))
  (is (thrown? AssertionError (c a :c {:b 1})))
  (is (df a ((pred> [b c])) ain [c] c))
  (is (= {:b 5 :c :hi} (c a :c {:b 5 :c :hi})))
  (is (thrown? AssertionError (c a :c 5)))
  (is (thrown? AssertionError (c a :c {:c 1})))
  (is (thrown? AssertionError (c a :b 5)))
  (is (thrown? AssertionError (c a :c {:b 1})))
  (is (df a ((pred> [b c])) ain [c] (assoc c :b 1 :c 1)))
  (is (= {:b 1 :c 1} (c a :c {:b 5 :c :hi})))
  (is (= {:b 1 :c 1} (a {:c {}})))
  (is (= {:b 1 :c 1} (a {:b 6 :c {}})))
  (is (thrown? AssertionError (a {:b 6})))
  (is (thrown? AssertionError (a [])))
  (is (thrown? AssertionError (c a :b 6)))
  (is (thrown? ClassCastException (c a :c 7)))
  (is (= {:b 1 :c 1 :z 6} (c a :c {:z 6}))))

(deftest df-output-keys-nested-validations
  (is (df a ((pred> [(++ :i (< 10)) b :k c])) "complex" ain [c] c))
  (is (= {:b 5 :c :hi} (c a :c {:b 5 :c :hi})))
  (is (thrown? AssertionError (a {:b :hi})))
  (is (thrown? AssertionError (c a :c 7)))
  (is (thrown? AssertionError (c a :c {})))
  (is (thrown? AssertionError (a {:c {:b 15 :c :hi}})))
  (is (thrown? AssertionError (a {:c {:b 5 :c 4}}))))

(deftest df-output-int-validations
  (is (df a (:i (or> #(< % 0) #(< 10 %))
                (ep> #(or (= 100 (+ 10 %)) (zero? (- % 20))
                          (zero? (- % 5)) (zero? (- % 50))
                          (zero? (- % 200)))
                      #(< % 100)))
          "complex"
          [b] b))
  (is (= 20 (c a :b 20)))
  (is (= 90 (a {:b 90})))
  (is (= 50 (a {:b 50})))
  (is (thrown? AssertionError (c a :c 200)))
  (is (thrown? AssertionError (c a :c 20)))
  (is (thrown? AssertionError (c a :b 60)))
  (is (thrown? AssertionError (c a :b 5)))
  (is (df b bin "simpler" (:f (< 10)) [c] c))
  (is (= 5.0 (c b :c 5.0)))
  (is (= -1.234 (c b :c -1.234)))
  (is (thrown? AssertionError (c b :c 5)))
  (is (thrown? AssertionError (c b :c 10.0)))
  (is (thrown? ClassCastException (c b :c [5]))))

(deftest df-input-int
  (is (df a [:i b] (* b 3)))
  (is (thrown? AssertionError (c a :b 1.2)))
  (is (thrown? AssertionError (c a :b -1.2)))
  (is (thrown? AssertionError (a {:b 1.2})))
  (is (= 9 (c a :b 3))))

(deftest df-input-float-default
  (is (df a "testing float" [:f [b 2.2 c 3.3 ]] (+ b c)))
  (is (df b bin "testing opt" [:f -c] (if c (* 2 c))))
  (is (= 5.5 (a {})))
  (is (= 5.5 (a {:something :else})))
  (is (= 4.4 (a {:b 1.1 :z 0})))
  (is (= 4.4 (c a :c 2.2)))
  (is (= 10.0 (c a :b 6.5 :c 3.5)))
  (is (= 10.0 (c a :b 7.0 :c 3.0)))
  (is (= 0.0 (a {:b 10.0 :c -10.0})))
  (is (= 4.0 (c b :c 2.0)))
  (is (thrown? AssertionError (c a :b 1)))
  (is (thrown? AssertionError (c a :b 1 :z 2.2)))
  (is (thrown? AssertionError (c a :c 4))))

(deftest df-pre-post
  (is (df a [b] {:pre [(= b 5)]} b))
  (is (thrown? AssertionError (c a :b 3)))
  (is (= 5 (c a :b 5)))
  (is (df a [b] {:post [(= b 5)]} b))
  (is (thrown? AssertionError (c a :b 3)))
  (is (= 5 (c a :b 5))))

(deftest pred-keys
  (is (pred a [b c -d [e 9]]))
  (is (not (false? (c a :b 1 :c 1))))
  (is (not (false? (c a :b 1 :c 1 :d 1 :e 1))))
  (is (false? (a {:b 1})))
  (is (false? (c a :b 1 :d 3 :e 5 :g 5))))

(deftest pred-int
  (is (pred a [:i b c]))
  (is (not (false? (c a :b 1 :c 2))))
  (is (false? (c a :b 2)))
  (is (false? (c a :c 2)))
  (is (false? (c a :b 1.1 :c 0.0)))
  (is (not (false? (a {:b 0 :c 0 :d 2.3 :e :hi})))))

(deftest pred-types
  (is (pred m [:i a :f b :k c :m d :b e :v f :i g]))
  (is (false? (m {})))
  (is (not (false? (c m :a 1 :b 1.0 :c :hi :d {1 2} :e true :f [:a] :g 1))))
  (is (false? (c m :a 1 :b 1.0 :c :hi :d {1 2} :e true :f [:a] :g 1.1)))
  (is (false? (c m :a 1 :b 1.0 :c :hi :d {1 2} :e true :f 2 :g 1.1)))
  (is (false? (c m :a 1 :b 1.0 :c :hi :d {1 2} :e true :f {} :g 1)))
  (is (false? (c m :a 1 :b 1.0 :c :hi :d {1 2} :e 2 :f [:a] :g 1)))
  (is (false? (c m :a 1 :b 1.0 :c :hi :d :hi :e true :f [:a] :g 1)))
  (is (false? (c m :a 1 :b 1.0 :c 1 :d {1 2} :e true :f [:a] :g 1)))
  (is (false? (c m :a 1 :b 1.0 :c :hi :d {1 2} :e true :f [:a])))
  (is (false? (c m :a 1.0 :b 1.0 :c :hi :d {1 2} :e true :f [:a] :g 1))))

(deftest pred-validations
  (is (pred m [(++ :i :n (> 20)) a b :f c -d]))
  (is (not (false? (c m :a 21 :b 1234567 :c -0.0))))
  (is (not (false? (c m :a 21 :b 1234567 :c -0.0 :d -111.111111))))
  (is (false? (c m :a 21 :b 1234567 :c -0.0 :d -1)))
  (is (false? (c m :a 21 :b 1234567 :c -0 :d -1.1)))
  (is (false? (c m :a 21 :b 1234567 :d -1)))
  (is (false? (c m :a 20 :b 1234567 :c -0.0 :d -1)))
  (is (false? (c m :a 21 :b 12 :c -0.0 :d -1)))
  (is (pred m [(or> #(= % 5) #(= 2.2 %) #(= :hi %) #(> 1000 %)) a -b c]))
  (is (not (false? (m {:a 5 :b :hi :c 999}))))
  (is (not (false? (m {:a 5 :c 999}))))
  (is (not (false? (m {:a 2.2 :c :hi}))))
  (is (not (false? (m {:a :hi :b :hi :d [] :c :hi}))))
  (is (not (false? (m {:a :hi :c 2.2}))))
  (is (false? (c m :a 5 :b 2)))
  (is (false? (c m :a 5 :b :hi)))
  (is (false? (c m :b :hi :c :hi)))
  (is (false? (c m :b :hi :c 1000)))
  (is (false? (c m :a 5 :b 2000 :c :hi)))
  (is (pred m [(++ :i (ep> #(> % 5) #(< % 10))) a b -c]))
  (is (not (false? (c m :a 6 :b 6 :c 6))))
  (is (false? (c m :a 6 :b 5 :c 6)))
  (is (false? (c m :a 6 :b 6.0 :c 6))))

(deftest pred-nested
  (is (pred m [(pred> [:i a :k b]) c]))
  (is (not (false? (c m :c {:a 1 :b :apples}))))
  (is (not (false? (c m :d [1 2] :c {:b :hi :a -100 :c {:h :hello}}))))
  (is (false? (c m :c {:a 1.0 :b :a})))
  (is (false? (c m :c {:a 1})))
  (is (false? (c m :c {:a 1 :c :hi})))
  (is (false? (c m :c {:a 1 :b 1})))
  (is (pred m [(pred> [(++ (< 10) :i) a :k b -z -v [e f g]]) c]))
  (is (not (false? (c m :c {:a 1 :b :h :z :h :v :hh :g :h}))))
  (is (not (false? (c m :c {:a 1 :b :apples}))))
  (is (not (false? (c m :d [1 2] :c {:b :hi :a -100 :c {:h :hello}}))))
  (is (false? (c m :c {:a 1.0 :b :a})))
  (is (false? (c m :c {:a 1 :b :h :z :h :v :hh :g 1})))
  (is (false? (c m :c {:a 1})))
  (is (false? (c m :c {:a 1 :c :hi})))
  (is (false? (c m :c {:a 1 :b 1}))))

(deftest traits
  (is (desc person [:str name :i age :k sex :n height]))
  (is (desc- tall [(> 2) height]))
  (is (desc tall-person [(and> person? tall?) tall-person-input]))
  (is (describe short-person [(person?) short-person-input (< 1) height]))
  (is (describe- tall-person-bobby [(tall-person?) tall-person-bobby-input (= "bobby") name]))
  (is (desc- child [(person?) child-input (< 27) age]))
  (is (desc short-child [(child?) short-child-input (< 0.8) height]))
  (is (not (false? (c tall-person? :name "andrew" :sex :m :age 95 :height 2.1))))
  (is (false? (c tall-person? :name "andrew" :sex :m :age 95 :height 2)))
  (is (false? (c tall-person-bobby? :name "bobby" :sex :m :age 7 :height 2)))
  (is (false? (c tall-person-bobby? :name "andrew" :sex :m :age 7 :height 3)))
  (is (false? (c tall-person-bobby? :name "bobby" :age 7 :height 3)))
  (is (not (false? (c tall-person-bobby? :name "bobby" :sex :m :age 7 :height 3))))
  (is (not (false? (c short-person? :name "bobby" :sex :m :age 7 :height 0.5))))
  (is (false? (c short-person? :name "bobby" :sex :m :age 7 :height 1.6)))
  (is (not (false? (short-child? {:name "alice" :age 15 :sex :f :height 0.5}))))
  (is (false? (short-child? {:name "alice" :age 15 :sex :f :height 1.5})))
  (is (false? (short-child? {:name "alice" :age 35 :sex :f :height 0.5})))
  (is (thrown? AssertionError (make-short-child {:name "andrew" :age 25 :sex :m})))
  (is (thrown? AssertionError (make-short-child {:name "andrew" :age 25 :height 1.5 :sex :m})))
  (is (def andrew (make-short-child {:name "andrew" :age 25 :height 0.5 :sex :m})))
  (is (not (false? (short-child? andrew))))
  (is (df make-tall ((tall-person?)) [(person?) make-tall-input]
          (assoc make-tall-input :height 3)))
  (is (df make-small (:m) [(person?) make-small-input]
          (assoc make-small-input :height 0.1)))
  (is (not (false? (tall-person? (make-tall andrew)))))
  (is (false? (tall-person? (make-small andrew)))))

(deftest collections
  (is (pred vex [(epcoll> #(pred> % [:i a b])) v]))
  (is (not (false? (c vex :v [{:a 1 :b 2} {:c 5 :a 99 :b -20}]))))
  (is (pred vex2 [(epcoll> (predfn [:i a b]) map?) v]))
  (is (not (false? (c vex2 :v [{:a 1 :b 2} {:c 5 :a 99 :b -20}]))))
  (is (pred multkey [(epcoll> (predfn [a b]) (predfn [c])) i]))
  (is (false? (c multkey :i [{:a 1 :b 3}])))
  (is (not (false? (c multkey :i [{:a 1 :b 3 :c 4}]))))
  (is (not (false? (c multkey :i [{:a 1 :b 3 :c 1} {:a :h :b :h :c :h}]))))
  (is (false? (c vex2 :v [5 []])))
  (is (false? (c vex :v [{:a 1 :b 2} {:c 5 :a 99 :b -20.0}])))
  (is (false? (c vex :v [{:a 1} {:c 5 :a 99 :b -20}])))
  (is (pred vex [(epcoll> #(pred> % [:i a b -c])) v w -z]))
  (is (not (false? (c vex :v [{:a 1 :b 2} {:c 5 :a 99 :b -20}]
                      :w [{:a 1 :b 2} {:c 5 :a 99 :b -20}]))))
  (is (not (false? (c vex :v [{:a 1 :b 2} {:c 5 :a 99 :b -20}]
                      :w [{:a 1 :b 2} {:c 5 :a 99 :b -20}]
                      :z [{:a -1 :b -1}]))))
  (is (not (false? (c vex :v [{:a 1 :b 2 :c 100} {:c 5 :a 99 :b -20}]
                      :w [{:a 1 :b 2} {:c 5 :a 99 :b -20}]))))
  (is (not (false? (c vex :v [{:a 1 :b 2} {:a 99 :b -20}]
                      :w [{:a 1 :b 2} {:c 5 :a 99 :b -20}]
                      :z [{:a -1 :b -1}]))))
  (is (false? (c vex :v [{:a 1 :b 2} {:a 99 :b -20}]
                      :w [{:a 1 :b 2} {:c 1.1 :a 99 :b -20}]
                      :z [{:a -1 :b -1}])))
  (is (false? (c vex :v [{:a 1 :b 2} {:c 5 :a 99 :b -20.0}])))
  (is (false? (c vex :v [{:a 1} {:c 5 :a 99 :b -20}]
                 :w [{:a 1 :b 2} {:c 5 :a 99 :b -20}])))
  (is (false? (c vex :v [{:a 1 :b 2} {:c 5 :a 99 :b -20}] :z 5
                 :w [{:a 1 :b 2} {:c 5 :a 99 :b -20}])))
  (is (false? (c vex :v [{:a 1 :b -1} {:c 5 :a 99 :b -20}] :z [{:a 1 :b 1.1}]
                 :w [{:a 1 :b 2} {:c 5 :a 99 :b -20}]))))

(deftest c>test
  (is (desc person [:str name spouse country]))
  (is (df married? [(person?) wife husband] (= (:name husband) (:spouse wife))))
  (is (pred dutch? [(person?) dutch?-input (= "netherlands" country)]))
  (is (pred married-dutch? in [(married?) in (dutch?) wife husband]))
  (is (pred married-dutch-c? [(c> married? :wife :husband husband) wife (dutch?) wife husband]))
  (is (def andrew (make-person {:name "andrew" :spouse "christine" :country "netherlands"})))
  (is (def bobby (make-person {:name "bobby" :spouse "alice" :country "netherlands"})))
  (is (def christine (make-person {:name "christine" :spouse "andrew" :country "netherlands"})))
  (is (not (false? (c married-dutch? :wife christine :husband andrew ))))
  (is (false? (married-dutch? {:c {:wife christine :husband bobby}})))
  (is (not (false? (c married-dutch-c? :wife christine :husband andrew ))))
  (is (false? (married-dutch-c? {:c {:wife christine :husband bobby}}))))

(deftest defaults
  (is (desc dude [:i [age (+ 10 15)]]))
  (is (desc dudes [[guy (d dude) guy2 (d dude)]]))
  (is (= {:guy {:age 25} :guy2 {:age 25}}) (d dudes)))
