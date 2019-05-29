
;;;======================================================
;;;   Auto Expert Sample Problem
;;;
;;;======================================================

(defmodule MAIN (export ?ALL))

;;*****************
;;* INITIAL STATE *
;;*****************

(deftemplate MAIN::attribute
   (slot name)
   (slot value)
   (slot certainty (default 100.0)))

(defrule MAIN::start
  (declare (salience 10000))
  =>
  (set-fact-duplication TRUE)
  (focus CHOOSE-QUALITIES Auto))

(defrule MAIN::combine-certainties ""
  (declare (salience 100)
           (auto-focus TRUE))
  ?rem1 <- (attribute (name ?rel) (value ?val) (certainty ?per1))
  ?rem2 <- (attribute (name ?rel) (value ?val) (certainty ?per2))
  (test (neq ?rem1 ?rem2))
  =>
  (retract ?rem1)
  (modify ?rem2 (certainty (/ (- (* 100 (+ ?per1 ?per2)) (* ?per1 ?per2)) 100))))
  
 
;;******************
;; The RULES module
;;******************

(defmodule RULES (import MAIN ?ALL) (export ?ALL))

(deftemplate RULES::rule
  (slot certainty (default 100.0))
  (multislot if)
  (multislot then))

(defrule RULES::throw-away-ands-in-antecedent
  ?f <- (rule (if and $?rest))
  =>
  (modify ?f (if ?rest)))

(defrule RULES::throw-away-ands-in-consequent
  ?f <- (rule (then and $?rest))
  =>
  (modify ?f (then ?rest)))

(defrule RULES::remove-is-condition-when-satisfied
  ?f <- (rule (certainty ?c1) 
              (if ?attribute is ?value $?rest))
  (attribute (name ?attribute) 
             (value ?value) 
             (certainty ?c2))
  =>
  (modify ?f (certainty (min ?c1 ?c2)) (if ?rest)))

(defrule RULES::remove-is-not-condition-when-satisfied
  ?f <- (rule (certainty ?c1) 
              (if ?attribute is-not ?value $?rest))
  (attribute (name ?attribute) (value ~?value) (certainty ?c2))
  =>
  (modify ?f (certainty (min ?c1 ?c2)) (if ?rest)))

(defrule RULES::perform-rule-consequent-with-certainty
  ?f <- (rule (certainty ?c1) 
              (if) 
              (then ?attribute is ?value with certainty ?c2 $?rest))
  =>
  (modify ?f (then ?rest))
  (assert (attribute (name ?attribute) 
                     (value ?value)
                     (certainty (/ (* ?c1 ?c2) 100)))))

(defrule RULES::perform-rule-consequent-without-certainty
  ?f <- (rule (certainty ?c1)
              (if)
              (then ?attribute is ?value $?rest))
  (test (or (eq (length$ ?rest) 0)
            (neq (nth 1 ?rest) with)))
  =>
  (modify ?f (then ?rest))
  (assert (attribute (name ?attribute) (value ?value) (certainty ?c1))))

;;*******************************
;;* CHOOSE WINE QUALITIES RULES *
;;*******************************

(defmodule CHOOSE-QUALITIES (import RULES ?ALL)
                            (import MAIN ?ALL))

(defrule CHOOSE-QUALITIES::startit => (focus RULES))

(deffacts the-rules

  ; Rules for picking the best mechanic

  (rule     (if preferred-mechanic is knocks )
        (then best-mechanic is knocks with certainty 30 ))
  (rule     (if preferred-mechanic is squeaks)
          (then best-mechanic is squeaks with certainty 30 ))
  (rule     (if preferred-mechanic is engine_speed )
          (then best-mechanic is engine_speed  with certainty 30 ))
  (rule        (if preferred-mechanic is unknown )
          (then best-mechanic is unknown  with certainty 30 ))


  ; Rules for picking the best vulcanizer

 (rule      (if preferred-vulcanizer is used)
         (then best-vulcanizer is used with certainty 30 ))
   (rule     (if preferred-vulcanizer is season )
           (then best-vulcanizer is season  with certainty 30 ))
   (rule        (if preferred-vulcanizer is pressure  )
           (then best-vulcanizer is  pressure with certainty 30 ))
   (rule         (if preferred-vulcanizer is unknown  )
           (then best-vulcanizer is unknown  with certainty 30 ))


  
  ; Rules for picking the best electrician


(rule      (if preferred-electrician is landing )
         (then best-electrician is landing with certainty 30 ))
   (rule     (if preferred-electrician is turns  )
           (then best-electrician is turns   with certainty 30 ))
   (rule        (if preferred-electrician is shines  )
           (then best-electrician is shines with certainty 30 ))
   (rule         (if preferred-electrician is unknown  )
           (then best-electrician is unknown  with certainty 30 ))



)

;;************************
;;* WINE SELECTION RULES *
;;************************

(defmodule Auto (import MAIN ?ALL)
                 (export deffunction get-list))

(deffacts any-attributes
  (attribute (name best-mechanic) (value any))
  (attribute (name best-vulcanizer) (value any))
  (attribute (name best-electrician) (value any)))

(deftemplate Auto::auto
  (slot name (default ?NONE))
  (multislot mechanic (default any))
  (multislot vulcanizer (default any))
  (multislot electrician (default any)))

(deffacts Auto::the-list
  (auto (name "Maks") (mechanic knocks)(vulcanizer unknown ) (electrician unknown))
  (auto (name "Maks") (mechanic squeaks)(vulcanizer unknown ) (electrician unknown))
  (auto (name "Maks") (mechanic engine_speed )(vulcanizer unknown ) (electrician unknown))

  (auto (name "Mariusz") (mechanic unknown )(vulcanizer used) (electrician unknown))
  (auto (name "Mariusz") (mechanic unknown )(vulcanizer season ) (electrician unknown))
  (auto (name "Mariusz") (mechanic unknown )(vulcanizer pressure ) (electrician unknown))

  (auto (name "Michal") (mechanic unknown )(vulcanizer unknown) (electrician landing ))
  (auto (name "Michal") (mechanic unknown )(vulcanizer unknown) (electrician turns ))
  (auto (name "Michal") (mechanic unknown )(vulcanizer unknown) (electrician shines ))

  (auto (name "Ekpert") (mechanic unknown )(vulcanizer unknown) (electrician unknown))


  (auto (name "Ekpert") (mechanic knocks)(vulcanizer used ) (electrician landing))
  (auto (name "Ekpert") (mechanic  squeaks)(vulcanizer season) (electrician turns))
  (auto (name "Ekpert") (mechanic  engine_speed)(vulcanizer pressure) (electrician shines)))

  
(defrule Auto::generate-auto
  (auto (name ?name)
        (mechanic $? ?c $?)
        (vulcanizer $? ?b $?)
        (electrician $? ?s $?))
  (attribute (name best-mechanic) (value ?c) (certainty ?certainty-1))
  (attribute (name best-vulcanizer) (value ?b) (certainty ?certainty-2))
  (attribute (name best-electrician) (value ?s) (certainty ?certainty-3))
  =>
  (assert (attribute (name auto) (value ?name)
                     (certainty (min ?certainty-1 ?certainty-2 ?certainty-3)))))

(deffunction Auto::auto-sort (?w1 ?w2)
   (< (fact-slot-value ?w1 certainty)
      (fact-slot-value ?w2 certainty)))
      
(deffunction Auto::get-list()
  (bind ?facts (find-all-facts ((?f attribute))
                               (and (eq ?f:name auto)
                                    (>= ?f:certainty 0))))
  (sort auto-sort ?facts))
  

