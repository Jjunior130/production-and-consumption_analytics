// Compiled by ClojureScript 1.8.51 {}
goog.provide('cljs.repl');
goog.require('cljs.core');
cljs.repl.print_doc = (function cljs$repl$print_doc(m){
cljs.core.println.call(null,"-------------------------");

cljs.core.println.call(null,[cljs.core.str((function (){var temp__4657__auto__ = new cljs.core.Keyword(null,"ns","ns",441598760).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(temp__4657__auto__)){
var ns = temp__4657__auto__;
return [cljs.core.str(ns),cljs.core.str("/")].join('');
} else {
return null;
}
})()),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Protocol");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m))){
var seq__12000_12014 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"forms","forms",2045992350).cljs$core$IFn$_invoke$arity$1(m));
var chunk__12001_12015 = null;
var count__12002_12016 = (0);
var i__12003_12017 = (0);
while(true){
if((i__12003_12017 < count__12002_12016)){
var f_12018 = cljs.core._nth.call(null,chunk__12001_12015,i__12003_12017);
cljs.core.println.call(null,"  ",f_12018);

var G__12019 = seq__12000_12014;
var G__12020 = chunk__12001_12015;
var G__12021 = count__12002_12016;
var G__12022 = (i__12003_12017 + (1));
seq__12000_12014 = G__12019;
chunk__12001_12015 = G__12020;
count__12002_12016 = G__12021;
i__12003_12017 = G__12022;
continue;
} else {
var temp__4657__auto___12023 = cljs.core.seq.call(null,seq__12000_12014);
if(temp__4657__auto___12023){
var seq__12000_12024__$1 = temp__4657__auto___12023;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__12000_12024__$1)){
var c__7667__auto___12025 = cljs.core.chunk_first.call(null,seq__12000_12024__$1);
var G__12026 = cljs.core.chunk_rest.call(null,seq__12000_12024__$1);
var G__12027 = c__7667__auto___12025;
var G__12028 = cljs.core.count.call(null,c__7667__auto___12025);
var G__12029 = (0);
seq__12000_12014 = G__12026;
chunk__12001_12015 = G__12027;
count__12002_12016 = G__12028;
i__12003_12017 = G__12029;
continue;
} else {
var f_12030 = cljs.core.first.call(null,seq__12000_12024__$1);
cljs.core.println.call(null,"  ",f_12030);

var G__12031 = cljs.core.next.call(null,seq__12000_12024__$1);
var G__12032 = null;
var G__12033 = (0);
var G__12034 = (0);
seq__12000_12014 = G__12031;
chunk__12001_12015 = G__12032;
count__12002_12016 = G__12033;
i__12003_12017 = G__12034;
continue;
}
} else {
}
}
break;
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m))){
var arglists_12035 = new cljs.core.Keyword(null,"arglists","arglists",1661989754).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_((function (){var or__6856__auto__ = new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m);
if(cljs.core.truth_(or__6856__auto__)){
return or__6856__auto__;
} else {
return new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m);
}
})())){
cljs.core.prn.call(null,arglists_12035);
} else {
cljs.core.prn.call(null,((cljs.core._EQ_.call(null,new cljs.core.Symbol(null,"quote","quote",1377916282,null),cljs.core.first.call(null,arglists_12035)))?cljs.core.second.call(null,arglists_12035):arglists_12035));
}
} else {
}
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"special-form","special-form",-1326536374).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Special Form");

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.contains_QMARK_.call(null,m,new cljs.core.Keyword(null,"url","url",276297046))){
if(cljs.core.truth_(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))){
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/"),cljs.core.str(new cljs.core.Keyword(null,"url","url",276297046).cljs$core$IFn$_invoke$arity$1(m))].join(''));
} else {
return null;
}
} else {
return cljs.core.println.call(null,[cljs.core.str("\n  Please see http://clojure.org/special_forms#"),cljs.core.str(new cljs.core.Keyword(null,"name","name",1843675177).cljs$core$IFn$_invoke$arity$1(m))].join(''));
}
} else {
if(cljs.core.truth_(new cljs.core.Keyword(null,"macro","macro",-867863404).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"Macro");
} else {
}

if(cljs.core.truth_(new cljs.core.Keyword(null,"repl-special-function","repl-special-function",1262603725).cljs$core$IFn$_invoke$arity$1(m))){
cljs.core.println.call(null,"REPL Special Function");
} else {
}

cljs.core.println.call(null," ",new cljs.core.Keyword(null,"doc","doc",1913296891).cljs$core$IFn$_invoke$arity$1(m));

if(cljs.core.truth_(new cljs.core.Keyword(null,"protocol","protocol",652470118).cljs$core$IFn$_invoke$arity$1(m))){
var seq__12004 = cljs.core.seq.call(null,new cljs.core.Keyword(null,"methods","methods",453930866).cljs$core$IFn$_invoke$arity$1(m));
var chunk__12005 = null;
var count__12006 = (0);
var i__12007 = (0);
while(true){
if((i__12007 < count__12006)){
var vec__12008 = cljs.core._nth.call(null,chunk__12005,i__12007);
var name = cljs.core.nth.call(null,vec__12008,(0),null);
var map__12009 = cljs.core.nth.call(null,vec__12008,(1),null);
var map__12009__$1 = ((((!((map__12009 == null)))?((((map__12009.cljs$lang$protocol_mask$partition0$ & (64))) || (map__12009.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__12009):map__12009);
var doc = cljs.core.get.call(null,map__12009__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists = cljs.core.get.call(null,map__12009__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name);

cljs.core.println.call(null," ",arglists);

if(cljs.core.truth_(doc)){
cljs.core.println.call(null," ",doc);
} else {
}

var G__12036 = seq__12004;
var G__12037 = chunk__12005;
var G__12038 = count__12006;
var G__12039 = (i__12007 + (1));
seq__12004 = G__12036;
chunk__12005 = G__12037;
count__12006 = G__12038;
i__12007 = G__12039;
continue;
} else {
var temp__4657__auto__ = cljs.core.seq.call(null,seq__12004);
if(temp__4657__auto__){
var seq__12004__$1 = temp__4657__auto__;
if(cljs.core.chunked_seq_QMARK_.call(null,seq__12004__$1)){
var c__7667__auto__ = cljs.core.chunk_first.call(null,seq__12004__$1);
var G__12040 = cljs.core.chunk_rest.call(null,seq__12004__$1);
var G__12041 = c__7667__auto__;
var G__12042 = cljs.core.count.call(null,c__7667__auto__);
var G__12043 = (0);
seq__12004 = G__12040;
chunk__12005 = G__12041;
count__12006 = G__12042;
i__12007 = G__12043;
continue;
} else {
var vec__12011 = cljs.core.first.call(null,seq__12004__$1);
var name = cljs.core.nth.call(null,vec__12011,(0),null);
var map__12012 = cljs.core.nth.call(null,vec__12011,(1),null);
var map__12012__$1 = ((((!((map__12012 == null)))?((((map__12012.cljs$lang$protocol_mask$partition0$ & (64))) || (map__12012.cljs$core$ISeq$))?true:false):false))?cljs.core.apply.call(null,cljs.core.hash_map,map__12012):map__12012);
var doc = cljs.core.get.call(null,map__12012__$1,new cljs.core.Keyword(null,"doc","doc",1913296891));
var arglists = cljs.core.get.call(null,map__12012__$1,new cljs.core.Keyword(null,"arglists","arglists",1661989754));
cljs.core.println.call(null);

cljs.core.println.call(null," ",name);

cljs.core.println.call(null," ",arglists);

if(cljs.core.truth_(doc)){
cljs.core.println.call(null," ",doc);
} else {
}

var G__12044 = cljs.core.next.call(null,seq__12004__$1);
var G__12045 = null;
var G__12046 = (0);
var G__12047 = (0);
seq__12004 = G__12044;
chunk__12005 = G__12045;
count__12006 = G__12046;
i__12007 = G__12047;
continue;
}
} else {
return null;
}
}
break;
}
} else {
return null;
}
}
});
