---
layout: page
title: "Funcional 3 + Desestruturação de Tuplas"
---

Em negrito: produções novas ou alteradas pela extensão.

{% include code-link.html path="functional3/Programa.java" %} ::= {% include code-link.html path="expressions2/expression/Expressao.java" %}

**Expressao** ::= {% include code-link.html path="expressions2/expression/Valor.java" %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpUnaria.java" %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpBinaria.java" %}\
{% include code-indent.html %} | {% include code-link.html path="functional2/expression/ExpDeclaracao.java" %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/Id.java" %}\
{% include code-indent.html %} | {% include code-link.html path="functional2/expression/Aplicacao.java" %}\
{% include code-indent.html %} | {% include code-link.html path="functional1/expression/IfThenElse.java" %}\
{% include code-indent.html %} | <strong>{% include code-link.html path="project/expression/ExpTupla.java" %}</strong>\
{% include code-indent.html %} | <strong>{% include code-link.html path="project/expression/ExpProjecao.java" %}</strong>

Valor ::= {% include code-link.html path="expressions2/expression/ValorConcreto.java" %}\
{% include code-indent.html %} | {% include code-link.html path="functional2/expression/ValorAbstrato.java" %}

ValorAbstrato ::= {% include code-link.html path="functional2/expression/ValorFuncao.java" %}

**ValorConcreto** ::= {% include code-link.html path="expressions2/expression/ValorInteiro.java" %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ValorBooleano.java" %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ValorString.java" %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ValorLista.java" %}\
{% include code-indent.html %} | <strong>{% include code-link.html path="project/expression/ValorTupla.java" %}</strong>

**ValorFuncao** ::= "fn" ListaPadrao "." {% include code-link.html path="expressions2/expression/Expressao.java" %}

**ExpTupla** ::= "(" {% include code-link.html path="expressions2/expression/Expressao.java" %} "," ListaExpressao ")"

**ExpProjecao** ::= {% include code-link.html path="expressions2/expression/Expressao.java" %} "." {% include code-link.html path="expressions2/expression/ValorInteiro.java" %}

ExpUnaria ::= {% include code-link.html path="expressions2/expression/ExpMenos.java" content='"-" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpNot.java" content='"not" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpLength.java" content='"length" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpHead.java" content='"head" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpTail.java" content='"tail" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpCompreensaoLista.java" %}

ExpCompreensaoLista ::= "[" {% include code-link.html path="expressions2/expression/Expressao.java" %} {% include code-link.html path="functional3/expression/Gerador.java" %} [ Filtro ] "]"

**Gerador** ::= "for" {% include code-link.html path="project/desestruturacao/Padrao.java" %} "in" {% include code-link.html path="expressions2/expression/Expressao.java" %}\
{% include code-indent.html %} | "for" {% include code-link.html path="project/desestruturacao/Padrao.java" %} "in" {% include code-link.html path="expressions2/expression/Expressao.java" %} [ "," ] {% include code-link.html path="functional3/expression/Gerador.java" %}

Filtro ::= "if" {% include code-link.html path="expressions2/expression/Expressao.java" %}

ExpBinaria ::= {% include code-link.html path="expressions2/expression/ExpSoma.java" content='Expressao "+" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpSub.java" content='Expressao "-" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpMult.java" content='Expressao "*" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpMaiorQue.java" content='Expressao ">" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpMenorQue.java" content='Expressao "<" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpAnd.java" content='Expressao "and" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpOr.java" content='Expressao "or" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpEquals.java" content='Expressao "==" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/ExpConcat.java" content='Expressao "++" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpSequencia.java" content='Expressao ".." Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpCons.java" content='Expressao ":" Expressao' %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/expression/ExpConcatLista.java" content='Expressao "^^" Expressao' %}

ExpDeclaracao ::= "let" {% include code-link.html path="functional1/declaration/DeclaracaoFuncional.java" %} "in" {% include code-link.html path="expressions2/expression/Expressao.java" %}

DeclaracaoFuncional ::= {% include code-link.html path="functional1/declaration/DecVariavel.java" %}\
{% include code-indent.html %} | {% include code-link.html path="functional2/declaration/DecFuncao.java" %}\
{% include code-indent.html %} | {% include code-link.html path="functional3/declaration/DecComposta.java" %}

**DecVariavel** ::= "var" {% include code-link.html path="project/desestruturacao/Padrao.java" %} "=" {% include code-link.html path="expressions2/expression/Expressao.java" %}

**DecFuncao** ::= "fun" {% include code-link.html path="expressions2/expression/Id.java" %} ListaPadrao "=" {% include code-link.html path="expressions2/expression/Expressao.java" %}

DecComposta ::= {% include code-link.html path="functional1/declaration/DeclaracaoFuncional.java" %} "," {% include code-link.html path="functional1/declaration/DeclaracaoFuncional.java" %}

IfThenElse ::= "if" {% include code-link.html path="expressions2/expression/Expressao.java" %} "then" {% include code-link.html path="expressions2/expression/Expressao.java" %} "else" {% include code-link.html path="expressions2/expression/Expressao.java" %}

Aplicacao ::= {% include code-link.html path="expressions2/expression/Expressao.java" %} "(" ListaExpressao ")"

ListaExpressao ::= {% include code-link.html path="expressions2/expression/Expressao.java" %}\
{% include code-indent.html %} | {% include code-link.html path="expressions2/expression/Expressao.java" %} "," ListaExpressao

**Padrao** ::= {% include code-link.html path="project/desestruturacao/PadraoId.java" %}\
{% include code-indent.html %} | {% include code-link.html path="project/desestruturacao/PadraoWildcard.java" %}\
{% include code-indent.html %} | {% include code-link.html path="project/desestruturacao/PadraoTupla.java" %}

**PadraoId** ::= {% include code-link.html path="expressions2/expression/Id.java" %}

**PadraoWildcard** ::= "_"

**PadraoTupla** ::= "(" {% include code-link.html path="project/desestruturacao/Padrao.java" %} "," ListaPadraoTupla ")"

**ListaPadraoTupla** ::= {% include code-link.html path="project/desestruturacao/Padrao.java" %}\
{% include code-indent.html %} | {% include code-link.html path="project/desestruturacao/Padrao.java" %} "," ListaPadraoTupla

**ListaPadrao** ::= {% include code-link.html path="project/desestruturacao/Padrao.java" %}\
{% include code-indent.html %} | {% include code-link.html path="project/desestruturacao/Padrao.java" %} ListaPadrao
