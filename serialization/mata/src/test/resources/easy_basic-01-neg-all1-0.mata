# example from https://github.com/VeriFIT/nfa-bench
@NFA-explicit
%Alphabet-auto
%Initial q0
%Final q4 q1
q0 0 q1
q0 1 q1
q1 0 q1
q1 0 q4
q1 1 q2
q2 0 q1
q2 0 q4
q2 1 q1
q2 1 q3
q3 0 q3
q3 1 q3
q4 0 q4
q4 1 q4
