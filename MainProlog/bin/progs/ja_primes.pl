/*
   Elegant prime number program by Jamie Andrews
   posted in comp.lang.prolog on 23 Apr 1999
   Generates one prime at a time, on backtracking

   Modified by Paul Tarau - to use difference list prime queue
*/
go:-go(8000).

go(N):-
  ctime(T1),
  primes_to(N,Ps),
  length(Ps,Ctr),
  ctime(T2),
  T is T2-T1,
  println([primes_to(N,Ctr),time(T)]),
  statistics.

primes_to(N,Ps):- 
  this_or_later_prime(2, Xs-Xs, P),
  P>N,
  !,
  append(Xs,[],Ys),
  !,
  Ps=Ys.

prime(P) :-
  this_or_later_prime(2, Xs-Xs, P).

this_or_later_prime(P, _, P).
this_or_later_prime(This, Previous_primes, P) :-
  N is This+1,
  % append newest prime at end because earlier primes will
  % detect composite numbers faster
  add_prime(This,Previous_primes,Previous_primes2),
  try_prime(N, Previous_primes2, P).

add_prime(X,Xs-[X|Ys],Xs-Ys).

try_prime(N, Previous, P) :-
  divisible_by_some(N, Previous),
  !,
  N1 is N+1,
  try_prime(N1, Previous, P).
try_prime(N, Previous, P) :-
  this_or_later_prime(N, Previous, P).

divisible_by_some(N, [P|_]-_) :-
  nonvar(P),
  0 is N mod P.
divisible_by_some(N, [_|Ps]-Qs) :-
  nonvar(Ps),
  divisible_by_some(N, Ps-Qs).
