## 1. Título do Projeto

**Desestruturação Recursiva de Tuplas na LF3 (e.g., `var (nome, (idade, _)) = pessoa`)**

## 2. Objetivo

Estender a **Linguagem Funcional 3 (LF3)** com **desestruturação de tuplas**, permitindo que um valor estruturado seja decomposto diretamente em variáveis no momento em que é ligado a um nome. A desestruturação deve ser recursiva (padrões podem conter outros padrões), aceitar wildcard (`_`) para posições que não interessam e funcionar da mesma forma em três lugares da linguagem: declarações de variável no `let`, parâmetros de função e geradores de compreensão de lista.

O projeto não tem como objetivo transformar tuplas em uma coleção cheia de operações. O que interessa aqui é a **semântica de binding**: dado um padrão e um valor (ou um tipo), decidir quais identificadores passam a existir, com qual valor e com qual tipo, e rejeitar de forma previsível tudo o que não se encaixa. Esse mecanismo precisa ser um só, compartilhado pela execução e pela checagem estática de tipos, e não uma regra especial escondida dentro do `let`.

## 3. Linguagem-Base

A LF3 estende a LF2 com listas e compreensão de listas. Um programa é uma expressão, funções são valores de primeira classe (`fun` e `fn`), declarações são introduzidas por `let ... in` e a checagem de tipos não usa anotações: os tipos dos parâmetros são inferidos a partir do uso, com a ajuda de `TipoPolimorfico`. Hoje, porém, todo ponto de binding da linguagem aceita apenas um identificador simples:

- `DecVariavel` guarda um único `Id`;
- `DefFuncao` guarda os parâmetros como `List<Id>`;
- `Gerador` liga um único `Id` a cada elemento da lista.

Além disso, a linguagem não tem tuplas. Os parênteses servem só para agrupar expressões ou para aplicar funções.

## 3.1. Tuplas como Valor (Pré-requisito)

Desestruturar tuplas pressupõe que elas existam. Antes de mexer nos pontos de binding, a LF3 precisa ganhar tuplas como valor, com a menor superfície possível:

- **Sintaxe**: `(e1, e2, ..., en)` com `n >= 2`. `(e)` continua sendo apenas agrupamento e `()` não é uma expressão válida.
- **Expressão e valor**: uma `ExpTupla` avalia cada componente, da esquerda para a direita, e produz um `ValorTupla` imutável. Tuplas são heterogêneas e podem conter listas, funções e outras tuplas.
- **Tipo**: um `TipoTupla` com a lista de tipos das posições, por exemplo `(string, (int, boolean))`.

Sintaxe sugerida:

```
ExpTupla ::= "(" Expressao "," ListaExpressao ")"
```

Mesmo sendo só o pré-requisito, essa parte já passa por várias camadas do interpretador. `ExpTupla` e `ValorTupla` precisam implementar todo o contrato de `Expressao` (`avaliar`, `checaTipo`, `getTipo`, `reduzir` e `clone`), e `ValorTupla.clone` precisa ser profundo, porque `Id.reduzir` insere cópias de valores dentro da árvore sintática. `TipoTupla` precisa responder corretamente a `eIgual`, `intersecao`, `eValido` e `getNome`, inclusive quando os componentes são `TipoPolimorfico` ainda não resolvidos. É isso que faz `[(1, 2), (3, 4)]` ser uma lista válida do tipo `[(int, int)]`, faz `[(1, 2), (3, "a")]` ser rejeitada e permite que um `if` com uma tupla em cada ramo tenha um tipo bem definido. O `main` do parser também precisa saber exibir um `ValorTupla` como resultado.

Nada além disso entra no escopo: não há acesso por índice, comparação de tuplas nem conversão entre tupla e lista (ver a seção 5.F).

---

## 4. Definição da Nova Sintaxe: Padrões de Desestruturação

A nova feature introduz **padrões** nos lugares onde a LF3 hoje espera um identificador sendo declarado. O exemplo central é este:

```
let var pessoa = ("Pessoa", (25, true)) in
    let var (nome, (idade, _)) = pessoa in
        (nome, idade)
```

O segundo `let` decompõe o valor recursivamente e cria apenas dois bindings:

```
nome  : string
idade : int
```

A posição marcada com `_` é validada (a segunda componente precisa ser uma tupla de duas posições), mas não gera nome nenhum. O resultado do programa é `("Pessoa", 25)`, do tipo `(string, int)`.

Os dois `let` aninhados não são capricho. Na LF3, declarações de um mesmo `let` são elaboradas em um ambiente auxiliar e não enxergam umas às outras, então `let var pessoa = ..., var (nome, (idade, _)) = pessoa in ...` falharia com variável não declarada. A desestruturação herda essa regra sem alterá-la.

### 4.1. Formas suportadas

| Forma                  | Exemplo                                  | Bindings criados                                    |
| :--------------------- | :--------------------------------------- | :-------------------------------------------------- |
| Simples                | `var (x, y) = (1, 2)`                    | `x : int`, `y : int`                                |
| Heterogênea            | `var (nome, idade) = ("Ana", 20)`        | `nome : string`, `idade : int`                      |
| Aridade variável       | `var (a, b, c, d) = (1, "b", true, [4])` | `a : int`, `b : string`, `c : boolean`, `d : [int]` |
| Aninhada               | `var (x, (y, z)) = (1, (2, 3))`          | `x`, `y` e `z`, todos `int`                         |
| Wildcard               | `var (x, _) = (1, 2)`                    | apenas `x : int`                                    |
| Parâmetro de função    | `fun soma (x, y) = x + y`                | `x` e `y` dentro do corpo                           |
| Retorno múltiplo       | `var (q, r) = divide(17, 5)`             | `q : int`, `r : int`                                |
| Gerador de compreensão | `[x + y for (x, y) in pares]`            | `x` e `y` a cada iteração                           |

### 4.2. Modelo conceitual: padrões

Para não espalhar lógica de desestruturação por `DecVariavel`, `DefFuncao`, `Aplicacao` e `Gerador`, a proposta é criar uma abstração própria de padrão, composta recursivamente:

```
Padrao
  +-- PadraoId
  +-- PadraoWildcard
  +-- PadraoTupla
        +-- Padrao
        +-- Padrao
        +-- ...
```

Com isso, `(x, (_, (y, z)))` deixa de ser um caso especial e passa a ser apenas uma composição de padrões. Uma interface mínima seria algo como:

```java
public interface Padrao {
    // liga os identificadores do padrão aos componentes do valor
    void bind(Valor valor, AmbienteExecucao ambiente);

    // faz o mesmo no ambiente de compilação, a partir de um tipo
    void bindTipo(Tipo tipo, AmbienteCompilacao ambiente);

    // árvore de tipos esperada pelo padrão (usada nos parâmetros de função)
    Tipo getTipoEsperado();

    // identificadores introduzidos, na ordem em que aparecem
    List<Id> getIdsLigados();

    Padrao clone();
}
```

A mesma abstração é reutilizada, sem adaptações, em três contextos:

```
let var Padrao = Expressao in Expressao

fun Id Padrao ... Padrao = Expressao

[ Expressao for Padrao in Expressao ]
```

Vale registrar que o código da LF3 já tem classes chamadas `Padrao`, `DecPadrao` e `ExpPadrao` (em `functional3.util` e `functional3.declaration`), além de uma segunda `ValorFuncao` em `functional3.expression`. Elas são resquícios de um casamento de padrões por valor em cláusulas de função e não estão ligadas ao parser atual. A abstração deste projeto é de outra natureza (padrões irrefutáveis, puramente estruturais) e deve ficar em um pacote próprio, por exemplo `lf3.plp.functional3.desestruturacao`, para não se misturar com esse código.

### 4.3. Por que não resolver com reescrita sintática

Uma saída tentadora seria traduzir `var (x, y) = e` para algo como `var t = e, var x = primeiro(t), var y = segundo(t)`. Isso não funciona bem na LF3 por vários motivos. A linguagem não tem projeção de tuplas (e acesso por índice está fora do escopo). Declarações do mesmo `let` não se enxergam, então `t` não estaria visível para `x` e `y`. O nome temporário precisaria ser gerado sem colidir com nomes do programa e sem interferir na redução parcial feita por `reduzir`. E os erros de aridade, estrutura e duplicidade seriam reportados sobre um código que o programador não escreveu. Nos parâmetros e nos geradores a reescrita seria ainda mais invasiva. Por isso a desestruturação é tratada como parte do mecanismo de binding da linguagem, com semântica própria na execução e na checagem de tipos.

---

## 5. Escopo Técnico e Implementação

O projeto altera o analisador léxico e sintático, a elaboração de declarações, a aplicação de funções, os geradores de compreensão, a redução parcial de expressões e o sistema de tipos. A integração entre essas partes é o centro do trabalho: um padrão que funciona no `let` mas não nos parâmetros, ou que executa corretamente mas não é tipado, não resolve o problema.

### A. Análise Léxica e Sintática (Parser)

- **Wildcard como token.** Na gramática JavaCC atual (`Funcional3.jj`), `_` é uma letra válida e sozinho já forma um `IDENTIFIER`. O wildcard precisa virar um token próprio, declarado antes de `IDENTIFIER`, de modo que `_` fique reservado e nomes como `_x` continuem sendo identificadores comuns. Em posição de expressão, `_` passa a ser erro de sintaxe (`var x = _` não é um programa válido).

- **Tupla versus agrupamento.** `( Expressao )` continua sendo agrupamento. Só existe tupla quando há pelo menos uma vírgula dentro dos parênteses.

- **A vírgula.** Na LF3 a vírgula já separa declarações de uma `DecComposta`, argumentos de uma aplicação, elementos de uma lista e, opcionalmente, geradores de uma compreensão. Agora ela também separa componentes de tuplas, tanto em expressões quanto em padrões, e as decisões de lookahead do parser precisam manter cada caso sem ambiguidade:
  - `f(1, 2)` continua sendo a aplicação de `f` a **dois** argumentos;
  - `f((1, 2))` é a aplicação de `f` a **um** argumento, que é uma tupla;
  - `f (1, 2)`, com espaço, é o mesmo que `f(1, 2)`, já que espaços não são significativos;
  - em `let fun g (x, y) = (y, x), var z = 1 in ...` as vírgulas internas pertencem ao padrão e à tupla, e a vírgula depois do `)` separa as declarações.

- **Padrões como parâmetros.** Hoje `fun` e `fn` recebem uma sequência de identificadores separados por espaço. Essa sequência passa a ser uma sequência de padrões, o que muda os tokens que podem iniciar um parâmetro (agora também `(` e `_`).

- **Expansão da Gramática (BNF):**

  ```bnf
  Programa ::= Expressao

  Expressao ::= Valor
              | ExpUnaria
              | ExpBinaria
              | ExpDeclaracao
              | Id
              | Aplicacao
              | IfThenElse
              | ExpTupla

  Valor ::= ValorConcreto
          | ValorAbstrato

  ValorAbstrato ::= ValorFuncao

  ValorConcreto ::= ValorInteiro
                  | ValorBooleano
                  | ValorString
                  | ValorLista

  ValorFuncao ::= "fn" ListaPadrao "." Expressao

  ExpTupla ::= "(" Expressao "," ListaExpressao ")"

  ExpUnaria ::= "-" Expressao
              | "not" Expressao
              | "length" Expressao
              | "head" Expressao
              | "tail" Expressao
              | ExpCompreensaoLista

  ExpCompreensaoLista ::= "[" Expressao Gerador [ Filtro ] "]"

  Gerador ::= "for" Padrao "in" Expressao
            | "for" Padrao "in" Expressao [ "," ] Gerador

  Filtro ::= "if" Expressao

  ExpBinaria ::= Expressao "+" Expressao
               | Expressao "-" Expressao
               | Expressao "*" Expressao
               | Expressao ">" Expressao
               | Expressao "<" Expressao
               | Expressao "and" Expressao
               | Expressao "or" Expressao
               | Expressao "==" Expressao
               | Expressao "++" Expressao
               | Expressao ".." Expressao
               | Expressao ":" Expressao
               | Expressao "^^" Expressao

  ExpDeclaracao ::= "let" DeclaracaoFuncional "in" Expressao

  DeclaracaoFuncional ::= DecVariavel
                        | DecFuncao
                        | DecComposta

  DecVariavel ::= "var" Padrao "=" Expressao

  DecFuncao ::= "fun" Id ListaPadrao "=" Expressao

  DecComposta ::= DeclaracaoFuncional "," DeclaracaoFuncional

  IfThenElse ::= "if" Expressao "then" Expressao "else" Expressao

  Aplicacao ::= Expressao "(" ListaExpressao ")"

  ListaExpressao ::= Expressao
                   | Expressao "," ListaExpressao

  Padrao ::= PadraoId
           | PadraoWildcard
           | PadraoTupla

  PadraoId ::= Id

  PadraoWildcard ::= "_"

  PadraoTupla ::= "(" Padrao "," ListaPadraoTupla ")"

  ListaPadraoTupla ::= Padrao
                     | Padrao "," ListaPadraoTupla

  ListaPadrao ::= Padrao
                | Padrao ListaPadrao
  ```

- **Resumo das mudanças em relação à LF3 atual:**

  | Produção              | Antes                            | Depois                               |
  | :-------------------- | :------------------------------- | :----------------------------------- |
  | `Expressao`           | sem tuplas                       | ganha `ExpTupla`                     |
  | `DecVariavel`         | `"var" Id "=" Expressao`         | `"var" Padrao "=" Expressao`         |
  | `DecFuncao`           | `"fun" Id ListaId "=" Expressao` | `"fun" Id ListaPadrao "=" Expressao` |
  | `ValorFuncao`         | `"fn" ListaId "." Expressao`     | `"fn" ListaPadrao "." Expressao`     |
  | `Gerador`             | `"for" Id "in" Expressao`        | `"for" Padrao "in" Expressao`        |
  | `Padrao` e subclasses | não existiam                     | novas                                |

### B. Semântica de Execução (Binding Recursivo)

Conceitualmente, todo binding da linguagem passa a ser uma chamada a:

```
bind(padrao, valor, ambiente)
```

O interpretador deve implementar as seguintes regras:

1. Se o padrão for um identificador, cria o binding correspondente no ambiente.
2. Se for `_`, a posição é aceita, mas nenhum binding é criado.
3. Se for uma tupla, verifica se o valor também é uma tupla com a mesma aridade.
4. Percorre recursivamente cada posição, da esquerda para a direita, ligando cada subpadrão ao componente correspondente.
5. Se houver incompatibilidade estrutural (o padrão espera uma tupla e o valor não é) ou de aridade, produz erro.
6. Antes de concluir, verifica se o mesmo identificador foi declarado mais de uma vez no padrão.

A regra 6 merece atenção. A verificação de duplicidade deve ser feita sobre o próprio padrão (via `getIdsLigados`), antes de qualquer valor ser ligado, e não pode depender do ambiente para descobrir o problema. O motivo é concreto: `Contexto.map` até lança exceção quando um nome se repete no mesmo bloco, mas `Aplicacao.resolveParametersBindings` monta os bindings dos parâmetros em um `HashMap<Id, Valor>` e `Gerador.checkTypeBindings` também acumula os tipos em um `HashMap`. Nesses caminhos, um `(x, x)` simplesmente sobrescreveria o primeiro valor, sem erro nenhum. Nos parâmetros, a verificação vale para a lista inteira: `fun f (x, y) x = ...` também é duplicidade.

Com a checagem de tipos aprovando o programa, os erros das regras 3 e 5 não deveriam aparecer em tempo de execução. Mesmo assim, a inferência da LF3 deixa passar valores cujo tipo ficou polimórfico, então o `bind` de execução mantém as verificações e lança erros específicos, em vez de deixar escapar um `ClassCastException`.

**Integração com o `let`.** `ExpDeclaracao` elabora as declarações em duas fases: `elabora` avalia as expressões no ambiente externo e grava os resultados em um ambiente auxiliar, e `incluir` copia os bindings do auxiliar para o ambiente do corpo. `DecVariavel.incluir` hoje copia exatamente um `Id`. Com padrões, precisa copiar todos os identificadores ligados, e isso vale para os dois ambientes (execução e compilação). A expressão do lado direito é avaliada uma única vez e só depois o valor é decomposto. Duplicidade entre declarações diferentes de uma mesma `DecComposta` continua sendo erro, do mesmo jeito que já acontece com `let var a = 1, var a = 2 in a`:

```
let var (a, b) = (1, 2), var a = 3 in a    // erro: identificador duplicado
```

**Redução parcial.** A LF3 faz redução parcial de expressões com `reduzir`: ao reduzir o corpo de uma função, cada `Id` conhecido no ambiente é substituído por uma cópia do seu valor, a menos que esteja mapeado para `ValorIrredutivel`. É por isso que `ValorFuncao.reduzir` marca o nome da função e cada parâmetro como irredutível antes de reduzir o corpo. Com padrões, **todos** os identificadores ligados por **todos** os padrões precisam ser marcados, e o mesmo cuidado vale para `Gerador.reduzir` e para a redução de declarações dentro de um `let`. Se algum ficar de fora, o identificador do padrão é capturado por uma variável externa de mesmo nome e o programa calcula outra coisa sem dar erro. A seção 6.2 tem um teste exatamente para isso.

### C. Integração com o Sistema de Tipos

A checagem estática é uma parte central do projeto. A estrutura da tupla determina os tipos dos identificadores criados durante a desestruturação:

```
var (nome, (idade, ativo)) = ("Pessoa", (25, true))

nome  : string
idade : int
ativo : boolean
```

Para isso existe uma segunda operação, espelho da primeira:

```
bindTipo(padrao, tipo, ambienteCompilacao)
```

1. `PadraoId`: mapeia o identificador para o tipo recebido.
2. `PadraoWildcard`: não mapeia nada.
3. `PadraoTupla` com `n` posições:
    - se o tipo for um `TipoPolimorfico` já resolvido, segue até o tipo que ele representa;
    - se o tipo ainda for uma variável de tipo livre, cria um `TipoTupla` com `n` novos `TipoPolimorfico`, unifica os dois e continua com a tupla recém-criada;
    - se for um `TipoTupla` com `m` posições e `m != n`, produz **erro de aridade**;
    - se for um `TipoTupla` com `n` posições, aplica `bindTipo` a cada subpadrão com o tipo da posição correspondente;
    - se for qualquer outro tipo (`int`, `string`, `boolean`, lista ou função), produz **erro estrutural**.

Esses erros devem ser detectados durante a checagem de tipos sempre que o tipo do lado direito for conhecido, e a mesma lógica vale recursivamente para tuplas aninhadas. O caso da variável de tipo livre é o que mais aparece na prática e o que dá mais trabalho. Ele surge, por exemplo, ao desestruturar o resultado de uma chamada recursiva. Dentro do próprio corpo, `DecFuncao.checaTipo` registra a função com um `TipoFuncao` cuja imagem é um `TipoPolimorfico` novo, então em

```
fun divide a b =
    if a < b then (0, a)
    else let var (q, r) = divide(a - b, b) in (q + 1, r)
```

o tipo de `divide(a - b, b)` ainda não é uma tupla quando `(q, r)` é checado. O `bindTipo` precisa construir essa estrutura e deixar a unificação resolver o resto: `q + 1` força `q` a ser `int`, e a interseção dos dois ramos do `if` amarra `r` ao tipo de `a`.

**A unificação da LF3 tem efeito colateral.** `TipoPolimorfico.eIgual` não apenas compara, ele instancia a variável de tipo. Com tipos simples isso é administrável. Com `TipoTupla` carregando variáveis de tipo em várias profundidades, o projeto precisa resolver três problemas que o código atual não trata:

- **Inferência do domínio.** `DefFuncao.getTipo` infere cada parâmetro com `((TipoPolimorfico) ambiente.get(id)).inferir()`. Um parâmetro `(x, (y, z))` não tem um `Id` que o represente. O domínio precisa ser reconstruído a partir da árvore devolvida por `getTipoEsperado`, cujas folhas são exatamente os mesmos objetos `TipoPolimorfico` que foram ligados a `x`, `y` e `z` no ambiente, e a inferência precisa descer recursivamente por essa árvore. Para `fun f (x, (y, z)) = x + y + z`, o tipo esperado é `((int, (int, int))) -> int`.
- **Limpeza de curingas.** Depois de cada aplicação, `TipoFuncao.limparTiposCuringas` apaga as instâncias dos curingas do domínio e da imagem, mas só no primeiro nível. Com tuplas, variáveis de tipo aninhadas guardariam a instância da chamada anterior e fariam a próxima aplicação falhar. A limpeza precisa ser recursiva.
- **Materialização do retorno.** `TipoFuncao.getTipo` resolve a instância da imagem apenas quando ela própria é um `TipoPolimorfico`. Se a imagem for uma tupla de curingas, como em `fun troca (a, b) = (b, a)`, o tipo devolvido precisa ser copiado com as instâncias já resolvidas **antes** da limpeza. Caso contrário, quem chamou recebe uma referência para a mesma estrutura que acabou de ser limpa e enxerga `(?, ?)`.

São esses três pontos que permitem que `troca((1, true))` e `troca(("x", 2))` convivam no mesmo programa, com tipos `(boolean, int)` e `(int, string)`. Também entram aqui a `intersecao` de tuplas, usada pelo `if` quando os dois ramos devolvem tuplas, e a verificação de homogeneidade de listas de tuplas, feita componente a componente.

### D. Desestruturação em Parâmetros de Função

Para que a desestruturação seja um mecanismo geral, e não um caso especial de declaração de variável, parâmetros de função também aceitam padrões:

```
let fun somaPar (x, y) = x + y in
    somaPar((10, 20))                  // 30

let fun f (x, (y, z)) = x + y + z in
    f((1, (2, 3)))                     // 6
```

Isso obriga a mudar a representação das funções. `DefFuncao` deixa de guardar `List<Id>` e passa a guardar uma lista de `Padrao`, e todos os pontos que dependem dessa lista mudam junto:

- `Aplicacao.avaliar` avalia cada argumento uma única vez e aplica o `bind` do padrão correspondente, acumulando os bindings antes de entrar no corpo;
- `DefFuncao.checaTipo` e `DefFuncao.getTipo` usam `getTipoEsperado` e `bindTipo` no lugar de um `TipoPolimorfico` por identificador;
- `TipoFuncao.checaTipo` passa a comparar argumentos com domínios que podem ser tuplas, o que torna `f(3)` um erro estrutural quando `f` espera `(x, y)`;
- `ValorFuncao.reduzir` marca como irredutíveis todos os identificadores ligados;
- `clone` e `toString` de `ValorFuncao` e `DecFuncao` passam a trabalhar com padrões.

A aridade da função continua sendo o número de padrões, e não o número de identificadores. `fun somaPar (x, y) = x + y` tem aridade 1: `somaPar((10, 20))` é válido e `somaPar(10, 20)` é erro de número de argumentos.

O retorno múltiplo é o outro lado da mesma ideia. Uma função devolve uma tupla e quem chama desestrutura o resultado, como no `divide` da seção anterior, que calcula quociente e resto só com subtração e recursão (a LF3 não tem operadores de divisão nem de resto).

Como `DecFuncao` é construída sobre `ValorFuncao`, funções anônimas recebem o mesmo tratamento sem esforço extra:

```
(fn (x, y) . x + y)((1, 2))            // 3
```

### E. Desestruturação em Compreensões de Lista

A LF3 já tem compreensão de listas com geradores no formato `for Id in Expressao`. Esse `for` não é um laço imperativo; ele faz parte da sintaxe da compreensão e, para cada elemento da lista, abre um escopo com o identificador ligado ao elemento. As coleções usadas nessa parte da LF3 são listas. Aqui o gerador passa a aceitar um padrão de tupla no lugar do identificador:

```
[x + y for (x, y) in [(1, 2), (3, 4), (5, 6)]]
// resultado: [3, 7, 11]

let var dados = [(1, (2, 10)), (3, (4, 20))] in
    [x + z for (x, (_, z)) in dados]
// resultado: [11, 23]
```

As mudanças no `Gerador` acompanham as das outras construções:

- `gerarValores` aplica o `bind` do padrão a cada elemento, dentro do escopo aberto para aquela iteração;
- `checkTypeBindings` aplica `bindTipo` com `TipoLista.getSubTipo()`, o que transforma `[x for (x, y) in [1, 2, 3]]` em erro estrutural já na checagem de tipos e resolve variáveis de tipo quando a lista vem de um parâmetro ainda polimórfico;
- `reduzir` marca como irredutíveis todos os identificadores do padrão.

Com isso, a mesma abstração de padrão fica em uso nos três contextos listados na seção 4.2, sem nenhuma regra de binding duplicada.

### F. Limites do Escopo

O projeto trabalha apenas com **padrões irrefutáveis**, como `(x, y)` e `(x, (_, z))`, cuja validade é decidida pela estrutura e, sempre que possível, pela checagem de tipos. Não implementa casamento de padrões dependente de valor, como `(0, y)`, que poderia falhar em tempo de execução e exigiria escolher entre alternativas.

Ficam explicitamente fora do escopo:

- `match`/`case` ou pattern matching geral;
- padrões que dependem de valores, como `(0, x)`;
- desestruturação de listas;
- rest/spread, como `(x, ...resto)`;
- tuplas nomeadas;
- tuplas mutáveis;
- acesso a tupla por índice;
- ordenação ou comparação geral de tuplas;
- conversão automática entre lista e tupla;
- guards em padrões.

---

## 6. Critérios de Aceitação

O projeto será aceito se a mesma abstração de padrão funcionar de forma consistente em declarações de variáveis, parâmetros de funções e compreensões de listas, tanto na execução quanto na checagem de tipos, e se os cenários abaixo se comportarem como descrito.

### 6.1. Casos Obrigatórios

| Código                                                | Resultado esperado                                      |
| :---------------------------------------------------- | :------------------------------------------------------ |
| `let var (x, y) = (1, 2) in x + y`                    | Válido, resulta em `3`                                  |
| `let var (x, y) = (1, 2, 3) in x`                     | Erro de aridade                                         |
| `let var (x, (y, z)) = (1, (2, 3)) in x + y + z`      | Válido, resulta em `6`                                  |
| `let var (x, (y, z)) = (1, 2) in x`                   | Erro estrutural                                         |
| `let var (x, (y, z)) = (1, (2, 3, 4)) in x`           | Erro de aridade na posição aninhada                     |
| `let var (x, y) = 5 in x`                             | Erro estrutural                                         |
| `let var (x, x) = (1, 2) in x`                        | Erro: identificador duplicado                           |
| `let var (x, (y, x)) = (1, (2, 3)) in x`              | Erro: identificador duplicado                           |
| `let var (_, x) = (1, 2) in x`                        | Válido, resulta em `2`                                  |
| `let var (_, _) = (1, 2) in true`                     | Válido (`_` não cria binding, então não há duplicidade) |
| `let fun f (x, (y, z)) = x + y + z in f((1, (2, 3)))` | Válido, resulta em `6`                                  |
| `let fun f (x, y) = x + y in f(3)`                    | Erro estrutural                                         |
| `let fun f (x, y) = x + y in f(1, 2)`                 | Erro de número de argumentos                            |
| `let fun f (x, y) x = x in f((1, 2), 3)`              | Erro: identificador duplicado                           |
| `let var (a, b) = (1, 2), var a = 3 in a`             | Erro: identificador duplicado                           |
| `[x for (x, y) in [1, 2, 3]]`                         | Erro estrutural                                         |
| `[(1, 2), (3, "a")]`                                  | Erro de tipo: elementos de tipos diferentes na lista    |
| `let var x = _ in x`                                  | Erro de sintaxe                                         |

Com exceção do erro de sintaxe, todos os erros da tabela devem ser detectados na checagem de tipos, antes de qualquer avaliação, com mensagens que deixem claro o tipo do problema (aridade, estrutura ou duplicidade).

### 6.2. Teste de Captura de Variáveis

```
let var x = 10 in
    let fun g p =
        let fun f (x, y) = x + y in f(p)
    in
        g((1, 2))
```

**Resultado Esperado:** `3`. Se o resultado for `12`, algum identificador ligado pelo padrão `(x, y)` não foi marcado como irredutível durante o `reduzir` do corpo de `g`, e o `x` do padrão acabou substituído pelo `x = 10` externo. A mesma verificação vale para funções anônimas: `let var x = 10 in (fn (x, y) . x + y)((1, 2))` também deve resultar em `3`.

### 6.3. Teste de Polimorfismo com Tuplas

```
let fun troca (a, b) = (b, a) in
    (troca((1, true)), troca(("x", 2)))
```

**Resultado Esperado:** `((true, 1), (2, "x"))`, com tipo `((boolean, int), (int, string))`. A primeira aplicação não pode deixar instâncias presas nas variáveis de tipo aninhadas, e o tipo devolvido por cada aplicação não pode ser afetado pela limpeza de curingas feita logo depois dela.

### 6.4. Teste de Retorno Múltiplo com Recursão

```
let fun divide a b =
        if a < b then (0, a)
        else let var (q, r) = divide(a - b, b) in (q + 1, r)
in
    let var (q, r) = divide(17, 5) in
        (r, q + r)
```

**Resultado Esperado:** `(2, 5)`, com tipo `(int, int)`, sabendo que `divide(17, 5)` vale `(3, 2)`. O programa só passa na checagem de tipos se o `bindTipo` souber desestruturar o tipo ainda não resolvido do resultado da chamada recursiva.

### 6.5. Teste de Integração Final

```
let
    fun transforma (x, (y, z)) =
        (x + y, z),

    var dados = [
        (1, (2, 10)),
        (3, (4, 20)),
        (5, (6, 30))
    ]
in
    [
        transforma((x, (y, z)))
        for (x, (y, z)) in dados
    ]
```

**Resultado Esperado:** `[(3, 10), (7, 20), (11, 30)]`, com tipo `[(int, int)]`.

Esse programa junta, em poucas linhas, tudo o que foi descrito: tuplas aninhadas como valor, padrão aninhado em parâmetro de função, padrão aninhado em gerador de compreensão, reconstrução de uma tupla a partir dos identificadores ligados e inferência do tipo da lista resultante. Repare que `z` não participa de nenhuma operação dentro de `transforma`, então o tipo da função é polimórfico nessa posição, e o `[(int, int)]` final só aparece se a materialização do tipo de retorno descrita na seção 5.C estiver correta. Uma entrega completa precisa avaliar e tipar corretamente programas desse nível, reutilizando o mesmo mecanismo de desestruturação em todos os pontos.
