# example from https://github.com/VeriFIT/nfa-bench
@NFA-bits
%Initial q0
%Final q1
q0 (!a1 & a2 & a3 & a4 & !a5) q3
q0 (a1 & !a2 & a3 & a4 & !a5) q3
q1 (a1 & a2 & a3 & a4 & a5) q1
q1 (!a1 & a2 & a3 & a4 & a5) q1
q1 (a1 & !a2 & a3 & a4 & a5) q1
q1 (a1 & a2 & !a3 & a4 & a5) q1
q1 (a1 & a2 & a3 & !a4 & a5) q1
q1 (!a1 & a2 & a3 & a4 & !a5) q1
q1 (a1 & !a2 & a3 & a4 & !a5) q1
q2 (!a1 & a2 & a3 & a4 & !a5) q1
q2 (a1 & !a2 & a3 & a4 & !a5) q1
q3 (!a1 & a2 & a3 & a4 & !a5) q2
q3 (a1 & !a2 & a3 & a4 & !a5) q2
