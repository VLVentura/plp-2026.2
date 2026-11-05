# Demo ao vivo: Desestruturação de Tuplas na LF3

Roteiro para copiar e colar na Web IDE, cerca de 10 minutos.

## Antes de começar

1. Em um terminal: `cd PLP/WebUI && npm start`.
2. Espere aparecer `Local: http://localhost:4004/` e abra esse endereço.
3. Selecione a linguagem **Project**.

## Como ler cada passo

- Cada bloco de código é um programa completo: cole e execute.
- **Resposta da IDE** é o texto exato que a IDE mostra.
- O **tipo** vem da checagem estática e só aparece na aba de ambiente de compilação, não na saída.
- Quando a checagem de tipos devolve apenas `false`, a IDE mostra só `erro de tipos!`. Isso acontece, por exemplo, com número errado de argumentos.

---

## 1. Tupla como valor

```text
(1, ("dois", true))
```

- Resposta da IDE: `(1, ("dois", true))`
- Tipo: `(int, (string, boolean))`
- O que mostra: a tupla é um valor de primeira classe, heterogêneo e aninhado.

## 2. Exemplo central

```text
let var pessoa = ("Pessoa", (25, true)) in
  let var (nome, (idade, _)) = pessoa in
    (nome, idade)
```

- Resposta da IDE: `("Pessoa", 25)`
- Tipo: `(string, int)`
- O que mostra: o padrão é recursivo, e o `_` confere a estrutura da posição sem criar binding. Os dois `let` estão aninhados porque declarações do mesmo `let` não se enxergam.

## 3. Erros no `let`, todos detectados na checagem de tipos

Aridade:

```text
let var (x, y) = (1, 2, 3) in x
```

- Resposta da IDE: `padrao (x, y) espera 2 posicoes, mas (INTEIRO, INTEIRO, INTEIRO) tem 3`

Estrutura, na posição aninhada:

```text
let var (x, (y, z)) = (1, 2) in x
```

- Resposta da IDE: `padrao (y, z) espera uma tupla, mas recebeu INTEIRO`

Duplicidade:

```text
let var (x, (y, x)) = (1, (2, 3)) in x
```

- Resposta da IDE: `identificador x aparece mais de uma vez no padrao`

Sintaxe:

```text
let var x = _ in x
```

- Resposta da IDE: `Encountered "" at line 1, column 1. Was expecting one of: ...`, que é um `ParseException`.
- O que mostra: `_` virou token próprio e só pode aparecer em padrões. A mensagem do JavaCC não aponta para o `_`, então vale avisar a plateia antes de rodar.

## 4. Padrão em parâmetro de função

```text
let fun f (x, (y, z)) = x + y + z in f((1, (2, 3)))
```

- Resposta da IDE: `6`
- Tipo de `f`: `(int, (int, int)) -> int`

A aridade é o número de padrões, e não o número de identificadores:

```text
let fun somaPar (x, y) = x + y in somaPar(10, 20)
```

- Resposta da IDE: `erro de tipos!`, porque são dois argumentos para uma função de aridade 1.

```text
let fun somaPar (x, y) = x + y in somaPar(3)
```

- Resposta da IDE: `parametro do tipo (INTEIRO, INTEIRO) espera uma tupla, mas recebeu INTEIRO`

## 5. Captura de variáveis na redução parcial

```text
let var x = 10 in
  let fun g p =
    let fun f (x, y) = x + y in f(p)
  in g((1, 2))
```

- Resposta da IDE: `3`
- O que mostra: se o `x` do padrão não fosse marcado como irredutível, seria trocado pelo `x = 10` externo e o resultado seria `12`.

## 6. Polimorfismo com tuplas

```text
let fun troca (a, b) = (b, a) in
  (troca((1, true)), troca(("x", 2)))
```

- Resposta da IDE: `((true, 1), (2, "x"))`
- Tipo: `((boolean, int), (int, string))`
- O que mostra: as variáveis de tipo aninhadas são limpas de forma recursiva entre as chamadas, e o retorno é materializado antes dessa limpeza.

## 7. Retorno múltiplo com recursão

```text
let fun divide a b =
      if a < b then (0, a)
      else let var (q, r) = divide(a - b, b) in (q + 1, r)
in
  let var (q, r) = divide(17, 5) in (r, q + r)
```

- Resposta da IDE: `(2, 5)`
- Tipo: `(int, int)`
- O que mostra: `bindTipo` desestrutura um tipo que ainda é uma variável livre (o retorno da chamada recursiva) criando uma tupla de variáveis novas.

## 8. Compreensões de lista

```text
let var dados = [(1, (2, 10)), (3, (4, 20))] in
  [x + z for (x, (_, z)) in dados]
```

- Resposta da IDE: `[11, 23]`
- Tipo: `[int]`

```text
[x for (x, y) in [1, 2, 3]]
```

- Resposta da IDE: `padrao (x, y) espera uma tupla, mas recebeu INTEIRO`

## 9. Projeção `e.k`

```text
let var p = ("Ana", (20, true)) in (p.1, p.2.2)
```

- Resposta da IDE: `("Ana", true)`
- Tipo: `(string, boolean)`

```text
(1, 2).3
```

- Resposta da IDE: `projecao .3 fora da tupla (INTEIRO, INTEIRO), que tem 2 posicoes (a primeira e .1)`

Quando o tipo da tupla ainda é livre, a projeção não sabe a aridade e é rejeitada:

```text
let fun f p = p.1 in f((1, 2))
```

- Resposta da IDE: `projecao .1 precisa de uma tupla de tipo conhecido, mas o tipo de p ainda nao foi inferido; use desestruturacao (let var (a, b) = ...)`

Desestruturar antes fixa a aridade, e aí a projeção é aceita:

```text
let fun f p = let var (a, b) = p in p.1 in f((1, 2))
```

- Resposta da IDE: `1`

## 10. Integração final

```text
let
  fun transforma (x, (y, z)) = (x + y, z),
  var dados = [(1, (2, 10)), (3, (4, 20)), (5, (6, 30))]
in
  [transforma((x, (y, z))) for (x, (y, z)) in dados]
```

- Resposta da IDE: `[(3, 10), (7, 20), (11, 30)]`
- Tipo: `[(int, int)]`
- O que mostra: tudo junto. Há padrão aninhado no parâmetro e no gerador, a função é polimórfica em `z`, e o tipo da lista só sai `[(int, int)]` porque o retorno é materializado.
