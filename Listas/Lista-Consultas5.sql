--É preciso atualizar a informação do saldo do cliente na tabela cliente.
--para este propósito devemos levar em conta o saldo dos depósitos menos os
--saldos de empréstimos. o cálculo final deve ser armazenado na tabela conta.

--CLIENTES QUE POSSUEM APENAS DEPOSITOS
--Q01:
SELECT nome_cliente,
       nome_agencia,
       numero_conta,
       SUM(saldo_deposito) AS total
FROM deposito
WHERE nome_cliente || nome_agencia || numero_conta NOT IN -- || é um operador de concatenação, e não um OU/OR lógico
      (SELECT nome_cliente || nome_agencia || numero_conta FROM emprestimo)
GROUP BY nome_cliente, nome_agencia, numero_conta;

--CLIENTES QUE POSSUEM APENAS EMPRESTIMOS
--Q02:
SELECT nome_cliente,
       nome_agencia,
       numero_conta,
       -1 * SUM(valor_emprestimo) AS total
FROM emprestimo
WHERE nome_cliente || nome_agencia || numero_conta NOT IN
      (SELECT nome_cliente || nome_agencia || numero_conta FROM deposito)
GROUP BY nome_cliente, nome_agencia, numero_conta;


--CLIENTES QUE POSSUEM DEPOSITO E EMPRESTIMOS (AMBOS)
--Q03:
SELECT e.nome_cliente,
       e.nome_agencia,
       e.numero_conta,
       SUM(d.saldo_deposito) - SUM(e.valor_emprestimo) AS total
FROM emprestimo AS e,
     deposito AS d
WHERE e.nome_cliente = d.nome_cliente
  AND e.nome_agencia = d.nome_agencia
  AND e.numero_conta = d.numero_conta
GROUP BY e.nome_cliente, e.nome_agencia, e.numero_conta;

--CLIENTES QUE POSSUEM DEPOSITOS, EMPRESTIMOS OU AMBOS
--Q04:
(SELECT nome_cliente,
        nome_agencia,
        numero_conta, --SOMENTE DEPOSITOS
        SUM(saldo_deposito) AS total
 FROM deposito
 WHERE nome_cliente || nome_agencia || numero_conta NOT IN
       (SELECT nome_cliente || nome_agencia || numero_conta FROM emprestimo)
 GROUP BY nome_cliente, nome_agencia, numero_conta)
UNION
(SELECT nome_cliente,
        nome_agencia,
        numero_conta, --SOMENTE EMPRESTIMOS
        -1 * SUM(valor_emprestimo) AS total
 FROM emprestimo
 WHERE nome_cliente || nome_agencia || numero_conta NOT IN
       (SELECT nome_cliente || nome_agencia || numero_conta FROM deposito)
 GROUP BY nome_cliente, nome_agencia, numero_conta)
UNION
(SELECT e.nome_cliente,
        e.nome_agencia,
        e.numero_conta, -- AMBOS
        SUM(d.saldo_deposito) - SUM(e.valor_emprestimo) AS total
 FROM emprestimo AS e,
      deposito AS d
 WHERE e.nome_cliente = d.nome_cliente
   AND e.nome_agencia = d.nome_agencia
   AND e.numero_conta = d.numero_conta
 GROUP BY e.nome_cliente, e.nome_agencia, e.numero_conta);

--Agora utilizamos a Q04 como relatório para o comando de atualização

UPDATE conta
SET saldo_conta = 0;
SELECT nome_cliente, saldo_conta
FROM conta
ORDER BY saldo_conta DESC;

--Q05:

UPDATE conta AS c
SET saldo_conta = relatorio.total
FROM ((SELECT nome_cliente,
              nome_agencia,
              numero_conta, --SOMENTE DEPOSITOS
              SUM(saldo_deposito) AS total
       FROM deposito
       WHERE nome_cliente || nome_agencia || numero_conta NOT IN
             (SELECT nome_cliente || nome_agencia || numero_conta FROM emprestimo)
       GROUP BY nome_cliente, nome_agencia, numero_conta)
      UNION
      (SELECT nome_cliente,
              nome_agencia,
              numero_conta, --SOMENTE EMPRESTIMOS
              -1 * SUM(valor_emprestimo) AS total
       FROM emprestimo
       WHERE nome_cliente || nome_agencia || numero_conta NOT IN
             (SELECT nome_cliente || nome_agencia || numero_conta FROM deposito)
       GROUP BY nome_cliente, nome_agencia, numero_conta)
      UNION
      (SELECT e.nome_cliente,
              e.nome_agencia,
              e.numero_conta, -- AMBOS
              SUM(d.saldo_deposito) - SUM(e.valor_emprestimo) AS total
       FROM emprestimo AS e,
            deposito AS d
       WHERE e.nome_cliente = d.nome_cliente
         AND e.nome_agencia = d.nome_agencia
         AND e.numero_conta = d.numero_conta
       GROUP BY e.nome_cliente, e.nome_agencia, e.numero_conta)) AS relatorio
WHERE c.nome_cliente = relatorio.nome_cliente
  AND c.nome_agencia = relatorio.nome_agencia
  AND c.numero_conta = relatorio.numero_conta;

--
-- PERCEBAM QUE ESTA CONSULTA AINDA NÃO FEZ UM RELATÓRIO COMPLETO
-- DOS CLIENTES DO BANCO PORQUE EXISTEM CLIENTES QUE NÃO FIZERAM
-- NEM EMPRESTIMOS E NEM DEPÓSITOS, MAS AINDA ASSIM POSSUEM UMA
-- CONTA NO BANCO.

-- EXISTE UM MODO MAIS FÁCIL DE REALIZAR ESTAS OPERAÇÕES?

-- COM A UTILIZAÇÃO DE JUNÇÕES, ESCREVEMOS CONSULTAS MENORES, MAIS
-- SIMPLES, DETERMINANDO COMO AS LINHAS DE CADA TABELA DEVEM SER
-- RETORNADAS DE ACORDO COM A EXISTÊNCIA OU NÃO DE UMA LINHA
-- CORRESPONDENTE NA OUTRA TABELA

--ANTES DE FAZER A CONSULTA COMPLETA, VAMOS GRADUAR A COMPLEXIDADE:
--COMEÇAMOS COM A CONSULTA PARA RETORNAR CLIENTES QUE POSSUEM
--EMPRESTIMOS E DEPOSITOS (AO MESMO TEMPO)

--Q06:
SELECT e.nome_cliente,
       e.nome_agencia,
       e.numero_conta,
       ROUND(SUM(d.saldo_deposito)) - ROUND(SUM(e.valor_emprestimo)) AS total
FROM emprestimo AS E
         INNER JOIN deposito AS D ON
            e.nome_cliente = d.nome_cliente
        AND e.nome_agencia = d.nome_agencia
        AND e.numero_conta = d.numero_conta
GROUP BY e.nome_cliente, e.nome_agencia, e.numero_conta;

-- UTILIZANDO A CLÁUSULA "NATURAL", ENTÃO A CLÁUSULA "ON" NÃO
-- NECESSITA SER UTILIZADA E A COMPLEXIDADE DA CONSULTA SERÁ
-- REDUZIDA COMPARANDO COM A CONSULTA INICIAL, NA QUAL TODAS
-- AS CHAVES DEVIAM SER COMPARADAS AOS PARES.
--REESCRITA POR MEIO DE JUNÇÕES. NESTE CASO TEMOS:
--Q07:
SELECT e.nome_cliente,
       e.nome_agencia,
       e.numero_conta,
       ROUND(SUM(d.saldo_deposito)) - ROUND(SUM(e.valor_emprestimo)) AS total
FROM emprestimo AS E
         NATURAL INNER JOIN deposito AS D
GROUP BY e.nome_cliente, e.nome_agencia, e.numero_conta;

--CONCLUSÃO:
--É MELHOR FAZER USO DA CLÁUSULA NATURAL QUANDO POSSÍVEL.

-- QUER-SE RETORNAR DADOS EXCLUSIVOS DA TABELA CLIENTE EM UM JOIN
-- COM A TABELA DEPOSITO. CASO EXISTAM DEPÓSITO DEVE SER MOSTRADO
-- E CASO NÃO EXISTAM DEPÓSITOS MOSTRAR APENAS OS DADOS DO CLIENTE

--Q08:

SELECT c.nome_cliente,
       c.cidade_cliente,
       d.nome_agencia,
       d.numero_conta,
       SUM(d.saldo_deposito) AS DEP
FROM cliente AS c
         NATURAL LEFT OUTER JOIN deposito AS d -- tabela da esquerda (cliente) de fora do join, exibindo mesmo se n houver correpondente na da direta (deposito)
GROUP BY c.nome_cliente, c.cidade_cliente, d.nome_agencia,
         d.numero_conta
ORDER BY c.nome_cliente, c.cidade_cliente, d.nome_agencia,
         d.numero_conta;

-- AS LINHAS DA TABELA À ESQUERDA QUE ESTIVEREM FORA DO JOIN
-- SERÃO RETORNADAS, PORÉM COM CONTEÚDO NULO NOS DADOS NÃO
-- AGRUPADOS.

--Q09: O MESMO ENTRE CLIENTE E EMPRESTIMO

SELECT c.nome_cliente,
       c.cidade_cliente,
       e.nome_agencia,
       e.numero_conta,
       SUM(e.valor_emprestimo) AS EMP
FROM cliente AS c
         NATURAL LEFT OUTER JOIN emprestimo AS e
GROUP BY c.nome_cliente, c.cidade_cliente, e.nome_agencia, e.numero_conta
ORDER BY c.nome_cliente, c.cidade_cliente, e.nome_agencia, e.numero_conta;

-- ISSO NÃO É UMA FALHA, MAS UMA VIRTUDE DO JOIN PORQUE NOS PERMITE
-- RESPONDER À SEGUINTE PERGUNTA:

--*************************************************************
-- "RETORNE TODOS OS CLIENTES DO BANCO COM SUAS RESPECTIVAS SOMAS
-- "DE DEPÓSITOS E EMPRESTIMOS CASO EXISTAM"
--*************************************************************
--Q10a:
SELECT c.nome_cliente,
       c.nome_agencia,
       c.numero_conta,
       SUM(d.saldo_deposito)   AS total_dep,
       SUM(e.valor_emprestimo) AS total_emp
FROM conta AS C
         NATURAL LEFT OUTER JOIN
     (emprestimo AS e NATURAL LEFT OUTER JOIN deposito AS d)
GROUP BY c.nome_cliente, c.nome_agencia, c.numero_conta;


--Q10b:
SELECT c.nome_cliente,
       c.nome_agencia,
       c.numero_conta,
       SUM(d.saldo_deposito)   AS total_dep,
       SUM(e.valor_emprestimo) AS total_emp
FROM conta AS c
         NATURAL FULL JOIN -- LEFT aqui já serve
    (emprestimo AS e NATURAL FULL JOIN deposito AS d) -- já aqui DEVE ser FULL
GROUP BY c.nome_cliente, c.nome_agencia, c.numero_conta;


--Agora a atualizacao do saldo da conta dos clientes fica
--mais simples do que a Q05
--Q10c ou Q05b:
UPDATE conta AS c
SET saldo_conta = relatorio.total
FROM (SELECT c.nome_cliente,
             c.nome_agencia,
             c.numero_conta,
             SUM(d.saldo_deposito) - SUM(e.valor_emprestimo) AS total
      FROM conta AS c
               NATURAL FULL JOIN
           (emprestimo AS e NATURAL FULL JOIN deposito AS d)
      GROUP BY c.nome_cliente, c.nome_agencia, c.numero_conta) AS relatorio
WHERE c.nome_cliente = relatorio.nome_cliente
  AND c.nome_agencia = relatorio.nome_agencia
  AND c.numero_conta = relatorio.numero_conta;

--SERÁ QUE AGORA ESTÁ CERTO?
SELECT nome_cliente, saldo_conta
FROM conta
ORDER BY saldo_conta DESC;

--Q10d
SELECT c.nome_cliente,
       c.nome_agencia,
       c.numero_conta,
       COALESCE(SUM(d.saldo_deposito), 0)   AS total_dep, -- caso o valor/argumento seja NULL, vai substituir por 0
       COALESCE(SUM(e.valor_emprestimo), 0) AS total_emp  -- null é retornando apenas se todos os argumentos forem NULL
FROM conta AS c
         NATURAL FULL JOIN
     (emprestimo AS E NATURAL FULL JOIN deposito AS d)
GROUP BY c.nome_cliente, c.nome_agencia, c.numero_conta;

--Q10e
SELECT c.nome_cliente,
       c.nome_agencia,
       c.numero_conta,
       COALESCE(SUM(d.saldo_deposito), 0) - COALESCE(SUM(e.valor_emprestimo), 0) AS total
FROM conta AS c
         NATURAL FULL JOIN
     (emprestimo AS e NATURAL FULL JOIN deposito AS d)
GROUP BY c.nome_cliente, c.nome_agencia, c.numero_conta;


--Q10f ou Q05c
UPDATE conta AS c
SET saldo_conta = relatorio.total
FROM (SELECT c.nome_cliente,
             c.nome_agencia,
             c.numero_conta,
             COALESCE(SUM(d.saldo_deposito), 0) - COALESCE(SUM(e.valor_emprestimo), 0) AS total
      FROM conta AS c
               NATURAL FULL JOIN
           (emprestimo AS e NATURAL FULL JOIN deposito AS d)
      GROUP BY c.nome_cliente, c.nome_agencia, c.numero_conta) AS relatorio
WHERE c.nome_cliente = relatorio.nome_cliente
  AND c.numero_conta = relatorio.numero_conta;

--SERÁ QUE AGORA ESTÁ CERTO?
SELECT nome_cliente, saldo_conta
FROM CONTA
ORDER BY saldo_conta DESC;
-- sim
