(ns mdparser-test.emitter
  (:require [cljs.test :refer-macros [deftest testing is]]
            [mdparser.parser :as parser]
            [mdparser.emitter :as emitter]))

(deftest test-operators
  (testing "testing basic operators"
    (is (= (emitter/emit 2 (parser/parse "x = y + z;"))
           "a['x']=(a['y']+a['z']);"))
    (is (= (emitter/emit 2 (parser/parse "x = y - z;"))
           "a['x']=(a['y']-a['z']);"))
    (is (= (emitter/emit 2 (parser/parse "x = y * z;"))
           "a['x']=(a['y']*a['z']);"))
    (is (= (emitter/emit 2 (parser/parse "x = y / z;"))
           "a['x']=div(a['y'],a['z']);"))
    (is (= (emitter/emit 2 (parser/parse "x = y & z;"))
           "a['x']=bitand(a['y'],a['z']);"))
    (is (= (emitter/emit 2 (parser/parse "x = y | z;"))
           "a['x']=bitor(a['y'],a['z']);")))
  (testing "tesing conditional operators"
    (is (= (emitter/emit 2 (parser/parse "x = y == z;"))
           "a['x']=((Math.abs((a['y'])-(a['z']))<0.00001)?1:0);"))
    (is (= (emitter/emit 2 (parser/parse "x = y != z;"))
           "a['x']=((Math.abs((a['y'])-(a['z']))<0.00001)?0:1);"))
    (is (= (emitter/emit 2 (parser/parse "x = y < z;"))
           "a['x']=((a['y']<a['z'])?1:0);"))
    (is (= (emitter/emit 2 (parser/parse "x = y > z;"))
           "a['x']=((a['y']>a['z'])?1:0);"))
    (is (= (emitter/emit 2 (parser/parse "x = y <= z;"))
           "a['x']=((a['y']<=a['z'])?1:0);"))
    (is (= (emitter/emit 2 (parser/parse "x = y >= z;"))
           "a['x']=((a['y']>=a['z'])?1:0);"))
    (is (= (emitter/emit 2 (parser/parse "x = y && z;"))
           "a['x']=((a['y']&&a['z'])?1:0);"))
    (is (= (emitter/emit 2 (parser/parse "x = y || z;"))
           "a['x']=((a['y']||a['z'])?1:0);"))))

(deftest test-functions
  (testing "testing basic functions"
    (is (= (emitter/emit 1 (parser/parse "x = rand(y);"))
           "a['x']=randint(a['y']);"))
    (is (= (emitter/emit 2 (parser/parse "x = rand(y);"))
           "a['x']=rand(a['y']);"))
    (is (= (emitter/emit 2 (parser/parse "x = pow(y, z);"))
           "a['x']=pow(a['y'], a['z']);"))
    (is (= (emitter/emit 2 (parser/parse "x = floor(y);"))
           "a['x']=Math.floor(a['y']);"))
    (is (= (emitter/emit 2 (parser/parse "x = max(y, z);"))
           "a['x']=Math.max(a['y'], a['z']);"))))

(deftest test-if
  (testing "simple if"
    (is (= (emitter/emit 2 (parser/parse "x = if(x,y,z);"))
           "a['x']=((Math.abs(a['x'])>0.00001)?(a['y']):(a['z']));")))
  (testing "complex if"
    (is (= (emitter/emit 2 (parser/parse "x = if(x / 3,y = w + c; y,z = w + k; w - 8);"))
            "a['x']=((Math.abs(div(a['x'],3))>0.00001)?((function(){a['y']=(a['w']+a['c']); return a['y']})()):((function(){a['z']=(a['w']+a['k']); return (a['w']-8)})()));"))))

(deftest test-numbers
  (testing "numbers"
    (testing "integers"
      (is (= (emitter/emit 2 (parser/parse "x = 1;"))
             "a['x']=1;"))
      (is (= (emitter/emit 2 (parser/parse "x = 123;"))
             "a['x']=123;"))
      (is (= (emitter/emit 2 (parser/parse "x = 001;"))
             "a['x']=1;")))
    (testing "decimals"
      (is (= (emitter/emit 2 (parser/parse "x = 1.0;"))
             "a['x']=1.0;"))
      (is (= (emitter/emit 2 (parser/parse "x = 12.345;"))
             "a['x']=12.345;"))
      (is (= (emitter/emit 2 (parser/parse "x = 1.;"))
             "a['x']=1.0;"))
      (is (= (emitter/emit 2 (parser/parse "x = .1;"))
             "a['x']=0.1;"))
      (is (= (emitter/emit 2 (parser/parse "x = 001.234;"))
             "a['x']=1.234;")))))

(deftest test-negative
  (testing "negative"
    (is (= (emitter/emit 2 (parser/parse "x = -1;"))
           "a['x']=-1;"))
    (is (= (emitter/emit 2 (parser/parse "x = --1;"))
           "a['x']=1;"))
    (is (= (emitter/emit 2 (parser/parse "x = ---1;"))
           "a['x']=-1;"))
    (is (= (emitter/emit 2 (parser/parse "x = 1--1;"))
           "a['x']=(1--1);"))
    (is (= (emitter/emit 2 (parser/parse "x = 1---1;"))
           "a['x']=(1-1);"))
    (is (= (emitter/emit 2 (parser/parse "x = -y;"))
           "a['x']=-a['y'];"))
    (is (= (emitter/emit 2 (parser/parse "x = --y;"))
           "a['x']=a['y'];"))
    (is (= (emitter/emit 2 (parser/parse "x = -if(x,y,z);"))
            "a['x']=-((Math.abs(a['x'])>0.00001)?(a['y']):(a['z']));"))
    (is (= (emitter/emit 2 (parser/parse "x = -(y * z);"))
           "a['x']=-(a['y']*a['z']);"))
    (is (= (emitter/emit 2 (parser/parse "x = --(y * z);"))
           "a['x']=(a['y']*a['z']);"))))

(deftest test-misc
  (testing "case insensitive"
    (is (= (emitter/emit 2 (parser/parse "x = RAND(Y);"))
           "a['x']=rand(a['y']);"))))
