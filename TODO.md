# TODO: Desestruturação de Tuplas na LF3

## Concluído

### Fase 1: Tuplas como valor
- [x] 1.1 Tupla como tipo (`TipoTupla`)
- [x] 1.2 Tupla como valor e na sintaxe: `(1, 2)` já funciona

### Fase 2: Padrões no `let`
- [x] 2.1 `let var (x, y) = (1, 2) in ...` funciona, incluindo aninhado e `_`
- [x] 2.2 Erros tratados: aridade errada, estrutura errada, nome repetido
- [x] 2.3 Desestruturar quando o tipo ainda não é conhecido (parâmetro de
      função, retorno de chamada recursiva)

## Pendente

### Fase 3: Padrões em parâmetros de função
- [ ] 3.1 Função recebendo tupla desestruturada direto no parâmetro:
      `fun f (x, y) = x + y`
- [ ] 3.2 A mesma função polimórfica funcionando com tuplas de tipos
      diferentes em chamadas distintas

### Fase 4: Padrões em compreensões de lista
- [ ] 4.1 `[x + y for (x, y) in lista]`

### Fase 5: Aceitação final
- [ ] 5.1 Rodar todos os casos de teste do README de ponta a ponta

## Notas rápidas
- Rodar os testes: `cd PLP && mvn -f Project/pom.xml test` (55 ok até agora)
- Tem uma segunda cópia da gramática pro WebDebug (IDE web) que precisa ser
  atualizada à mão sempre que mexemos no parser
