% indexed dynamic database layer

% clause table

iclauses_clean(Db):-
  iclause_iterate(Db,Ref),
  iclause_rm(Db,Ref),
  fail.
iclauses_clean(Db):-  
  init_gensym(Db).

new_iclause(Db,HB,Ref):-
  gensym_no(Db,Ref),
  def(Db,Ref,HB).
 
iclause_get(Db,Ref,HB):-
  val(Db,Ref,HB).

iclause_rm(Db,Ref):-
  rm(Db,Ref).
  
iclause_all(Db,Ref,HB):-
  iclause_iterate(Db,Ref),
  val(Db,Ref,HB).

iclause_iterate(Db,Ref):-
  val(gensym,Db,Last),
  for(Ref,1,Last).
    
iclauses_show(Db):-
  iclause_all(Db,_,HB),
  pp_clause(HB),
  fail.
iclauses_show(_). 

% discovery of indexable args

arg_path_of(H,I,G):-
  argn(I,H,A),
  functor(A,G,_).

% discovery of non-indexable args

var_path_of(H,I):-
  argn(I,H,A),
  var(A).
  
named_dict(DictName,Dict):-val('$dict',DictName,Dict),!.
named_dict(DictName,Dict):-new_dict(Dict),def('$dict',DictName,Dict).

root_dict(Dict):-named_dict('$root',Dict).

% if K is known, checks if it is a key, if not generates them all
dict_key(D,K):-nonvar(K),!,dict_get(D,K,V),V\=='$null'.
dict_key(D,K):-
  invoke_java_method(D,getKeys,Iter),
  iterator_element(Iter,K).
  
dict_child_of(ParentDict,DbName,Dict):-
  dict_key(ParentDict,DbName),
  % this will not insert a new child as 
  % dict_key only enumerates existing
  dict_get(ParentDict,DbName,Dict).  

% adding to the index

idb_index(Db,HB,Ref):-
  Locked= -1,
  ( nonvar(Db),HB=(H:-_),functor(H,F,N)->
    true
  ; !,errmes(instatiation_error,adding_to(Db,Ref:HB))
  ),
  root_dict(RD),
  dict_ensure_child(RD,Db,D),
  dict_ensure_child(D,F,DF),
  dict_ensure_child(DF,N,DFN),
  dict_ensure_child(DFN,0,DF0),
  dict_put(DF0,Ref,0),
  ( arg_path_of(H,I,G),
      (dict_get(DFN,I,Val),Val==Locked->fail;true),
      dict_ensure_child(DFN,I,DFI),
      dict_ensure_child(DFI,G,DG), % G->G/M
      dict_put(DG,Ref,I),
    fail
  ;
    var_path_of(H,I),
      % lock index position on first var arg
      (dict_get(DFN,I,DFI)->Found=true;Found=fail),
      dict_put(DFN,I,Locked),
      (Found->delete_java_object(DFI);true), % $$ bug in new_dict?
    fail
  ; 
    true
  ).

% retrieval from the index

idb_get_index(DbName,(H:-_),Ref):-
  Locked= -1,
  root_dict(RD),
  dict_child_of(RD,DbName,D),
    %tab(2),println(trace_entering_db(DbName,H)),
  (functor(H,F,N)->true;true),
  dict_child_of(D,F,DF),
  dict_child_of(DF,N,DFN),
  (var(H)->functor(H,F,N);true),
    %tab(4),println(trace_entering_pred(F/N=>DF)),
  ( arg_path_of(H,I,G),
      %tab(6),println(trace_entering_arg_path(F/N=>I:G)),
    dict_child_of(DFN,I,DFI),
    DFI\==Locked
    ->
      %tab(6),println(trace_finding_arg(i=I,g=G)),
    dict_child_of(DFI,G,DG),
       %tab(8),println(trace_entering_fun(i=I,g=G,H)),
    dict_child_of(DG,Ref,J),
        %tab(10),println(trace_entering_ind(i=I,j=J,g=G,ref=Ref)),
    I=J
  ; dict_child_of(DFN,0,DF0),
      %tab(8),println(trace_entering_fun_any(F/N)),
    dict_child_of(DF0,Ref,K),
       %tab(10),println(trace_entering_any(F/N,ref=Ref)),
    K=0
  ).
          
idb_assert(Db,HB, Ref):-
  new_iclause(Db,HB,Ref),
  idb_index(Db,HB,Ref),
  % println(trace_asserting(Ref,HB)),
  true.

idb_asserted(DbName,HB,Ref):-
  idb_get_index(DbName,HB,Ref),
  % println(trace_here(Ref:HB)),
  iclause_get(DbName,Ref,HB),
  % println(trace_asserted(Ref:HB)),
  true.

idb_clause(DbName,H,B):-idb_asserted(DbName,(H:-B),_Ref).

% idb_is_dynamic(DbName,H):- \+(\+(idb_clause(DbName,H,_))). 
 
idb_is_dynamic(DbName,H):-
  nonvar(DbName),
  root_dict(RD),
  dict_get(RD,DbName,D),
  D\=='$null',
  functor(H,F,N),
  dict_get(D,F,DF),
  DF\=='$null',
  dict_get(DF,N,DFN),
  DFN\=='$null'.

idb_consult(File,Db):-
  foreach(
    clause_of(File,C),
    idb_consult_action(C,Db)
  ).

idb_consult_action(':-'(G),Db):-if(G=[F],idb_consult(F,Db),once(idb_call(Db,G))).
idb_consult_action(':-'(H,B),Db):-idb_assert(Db,':-'(H,B),_).

idb_reconsult(File,Db):-
  db_clean(Db),
  idb_consult(File,Db).

idb_listing:-idb_listing(_).

idb_listing(Db):-
  idb_listing(Db,_).
  
idb_listing(DbName,F/N):-
  (HB=(H:-_),functor(H,F,N)->true;true),
  idb_asserted(DbName,HB,Ref),
  write_chars("/*"),write(Ref),write_chars("*/ "),
  pp_clause(HB),
  fail.
idb_listing(_,_).  

idb_show_index:-idb_show_index(_).

idb_show_index(DbName):-
  println('INDEX'),
  HB=(H:-_),
  foreach(
    idb_show_index(DbName,F,N,I,_,G,Ref,HB),
    ( functor(H,F,N),HI=((F/N)->[I]->G),
      println(index(DbName,Ref):HI=>H)
    )
  ).

idb_show_index(DbName,F,N,I,J,G,Ref,HB):-
  Locked= -1,
  root_dict(RD),
  dict_child_of(RD,DbName,D),
  dict_child_of(D,F,DF),
  dict_child_of(DF,N,DFN),
  dict_child_of(DFN,I,DFI),
  %tab(2),println(index_obs:[RD,D,DF,DFN,DFI]),
  %tab(2),println(index_for(F/N-i(I),dfi(DFI))),
  ( I=:=0->dict_child_of(DFI,Ref,J)
  ; DFI\==Locked,dict_child_of(DFI,G,DG),
    dict_child_of(DG,Ref,J)
  ),
  iclause_get(DbName,Ref,HB).
  
idb_clean(DbName):-
  % todo - visit recursively and free symtable obs
  root_dict(RD),
  iclauses_clean(DbName),
  dict_remove(RD,DbName).

idb_call(Db,Body):-idb_body(Db,Body).

idb_body(Db,Body):-var(Body),!,errmes(bad_metacall(Db),var(Body)).
idb_body(Db,Body) :-
	idb_body(Db,Body, AfterCut, HadCut),
	( HadCut = yes,
		!,
		idb_body(Db,AfterCut)
	;   HadCut = no
	).

idb_body(_Db,(!,AfterCut), AfterCut, yes) :- !.
idb_body(Db,(Goal,Body), AfterCut, HadCut) :- !,
	idb_goal(Db,Goal),
	idb_body(Db,Body, AfterCut, HadCut).
idb_body(_Db,!, true, yes).
idb_body(Db,(Disj1;_), AfterCut, HadCut) :-
	idb_body(Db,Disj1, AfterCut, HadCut).
idb_body(Db,(_;Disj2), AfterCut, HadCut) :- !,
	idb_body(Db,Disj2, AfterCut, HadCut).
idb_body(Db,Goal, true, no) :-
	idb_goal(Db,Goal).

idb_goal(_Db,Goal) :-
	is_compiled(Goal), % <--- check for a compiled predicate
	!,
	%println('calling compiled'(Goal)),
	Goal.
idb_goal(Db,Goal) :-
	idb_is_dynamic(Db,Goal),
  !,
	idb_clause(Db,Goal, Body),	% <--- assume anything else is interpreted
	idb_body(Db,Body, AfterCut, HadCut),
	(	HadCut = yes,
		!,
		idb_body(Db,AfterCut)
	;	HadCut = no
	).
idb_goal(Db,Undef):-
  idb_undefined(Db,Undef).
  
idb_undefined(Db,Undef):-
  errmes(undefined_predicate_in_call(Db),Undef).

% this DB

idb_assert(HB):-
  this_db(Db),
  to_clause(HB,C),
  idb_assert(Db,C,_Ref).

idb_clause(H,B):-
  this_db(Db),
  idb_clause(Db,H,B).
    
idb_call(G):-
  this_db(Db),
  idb_call(Db,G).

idb_consult(File):-
  this_db(Db),
  idb_consult(File,Db).
  
idb_reconsult(File):-
  this_db(Db),
  idb_reconsult(File,Db).
