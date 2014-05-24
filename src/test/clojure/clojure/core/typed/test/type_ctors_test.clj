(ns clojure.core.typed.test.type-ctors-test
  (:require [clojure.test :refer :all]
            [clojure.core.typed.test.test-utils :refer :all]
            [clojure.core.typed.type-ctors :refer :all]
            [clojure.core.typed.type-rep :refer :all]
            [clojure.core.typed.parse-unparse :refer [parse-type]]))

(defmacro overlap-prs [s1 s2]
  `(clj
     (overlap (parse-type '~s1) (parse-type '~s2))))

(deftest overlap-test
  (is-clj (not (overlap -false -true)))
  (is-clj (not (overlap (-val :a) (-val :b))))
  (is-clj (overlap (RClass-of Number) (RClass-of Integer)))
  (is-clj (not (overlap (RClass-of Number) (RClass-of clojure.lang.Symbol))))
  (is-clj (not (overlap (RClass-of Number) (RClass-of String))))
  (is-clj (overlap (RClass-of clojure.lang.Seqable [-any]) (RClass-of clojure.lang.IMeta)))
  (is-clj (overlap (RClass-of clojure.lang.Seqable [-any]) (RClass-of clojure.lang.PersistentVector [-any]))))

(deftest hmap-overlap-test
  (is-clj
    (not (overlap-prs Integer clojure.lang.Keyword)))
  (is-clj
    (not
      (overlap-prs
        (HMap :mandatory {:a Integer})
        (HMap :mandatory {:a clojure.lang.Keyword}))))
  (is-clj
    (overlap-prs
      (HMap :optional {:a Integer})
      (HMap :optional {:a clojure.lang.Keyword})))
  (is-clj
    (overlap-prs
      (HMap :complete? true :optional {:a Integer})
      (HMap :complete? true :optional {:a clojure.lang.Keyword}))))

(deftest hvec-overlap-test
  (testing "without rest types"
    (testing "when the fixed types match"
      (is-clj
       (overlap-prs
        (HVec [Number])
        (HVec [Number]))))

    (testing "when the fixed types differ"
      (is-clj
       (not
        (overlap-prs
         (HVec [Number])
         (HVec [String])))))

    (testing "with a differing number of fixed types"
      (is-clj
       (not
        (overlap-prs
         (HVec [Number])
         (HVec [Number String]))))))

  (testing "with one rest type"
    (testing "when fixed types match"
      (is-clj
       (overlap-prs
        (HVec [Number])
        (HVec [Number String *]))))

    (testing "when fixed types differ"
      (is-clj
       (not
        (overlap-prs
         (HVec [Number])
         (HVec [String String *])))))

    (testing "when the extra fixed types match the rest type"
      (is-clj
       (overlap-prs
        (HVec [Number *])
        (HVec [Number]))))

    (testing "when the extra fixed types differ from the rest type"
      (is-clj
       (not
        (overlap-prs
         (HVec [Number *])
         (HVec [String])))))

    (testing "when the extra fixed types come from type with the rest type"
      (is-clj
       (not
        (overlap-prs
         (HVec [String String String *])
         (HVec [String]))))))

  (testing "with two rest types"
    (testing "when the rest types match"
      (is-clj
       (overlap-prs
        (HVec [Number *])
        (HVec [Number *]))))

    (testing "when the rest types differ"
      (is-clj
       (not
        (overlap-prs
         (HVec [Number *])
         (HVec [String *])))))

    (testing "when the extra fixed types match the rest type of shorter"
      (is-clj
       (overlap-prs
        (HVec [Number *])
        (HVec [Number Number *]))))

    (testing "when the extra fixed types differ from the rest type of shorter"
      (is-clj
       (not
        (overlap-prs
         (HVec [Number *])
         (HVec [String Number *])))))

    (testing "when the fixed types match"
      (is-clj
       (overlap-prs
        (HVec [Number String *])
        (HVec [Number String *]))))

    (testing "when the fixed types differ"
      (is-clj
       (not
        (overlap-prs
         (HVec [Number String *])
         (HVec [String String *])))))))
