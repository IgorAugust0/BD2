--01-Nome e cidade de clientes que possuem algum emprestimo, ordenado pelo nome do cliente

SELECT DISTINCT cliente.nome_cliente, cidade_cliente
FROM emprestimo,
     cliente
WHERE cliente.nome_cliente = emprestimo.nome_cliente
ORDER BY cliente.nome_cliente;

--02-Nome e cidade de clientes que possuem emprestimo na PUC, ordenado pelo nome do cliente

SELECT DISTINCT cliente.nome_cliente, cidade_cliente
FROM cliente,
     emprestimo
WHERE cliente.nome_cliente = emprestimo.nome_cliente
  AND nome_agencia = 'PUC'
ORDER BY cliente.nome_cliente;

--03-Nomes de Clientes com saldo entre 100 e 900

SELECT cliente.nome_cliente
FROM cliente,
     deposito
WHERE cliente.nome_cliente = deposito.nome_cliente
  AND deposito.saldo_deposito BETWEEN 100 AND 900;

--04-Nomes de clientes, valores de depósitos e empréstimos na agencia PUC
SELECT d.nome_cliente, d.saldo_deposito, e.valor_emprestimo, e.nome_agencia
FROM deposito AS d,
     emprestimo AS e
WHERE d.nome_agencia = 'PUC'
  AND d.nome_agencia = e.nome_agencia
  AND d.nome_cliente = e.nome_cliente;

--05-Selecione os nomes dos clientes da cidade de Contagem com depósitos maiores do que R$ 3.000,00

SELECT c.nome_cliente, c.cidade_cliente, d.saldo_deposito
FROM cliente AS c,
     deposito AS d
WHERE c.nome_cliente = d.nome_cliente -- verifica a correspondencia do cliente entre as duas tabelas se é de fato, o mesmo
  AND c.cidade_cliente = 'Contagem'
  AND d.saldo_deposito > 3000;

--06-Um gerente pretende criar uma lista de clientes que correm o risco
--de se individar de maneira irreversível. Para tanto ele formulou
--a seguinte pesquisa:
--Selecione os clientes da cidade de Santa Luzia com depósitos
--menores do que R$ 1.000,00 e emprestimos maiores que R$ 1.000,00

SELECT c.nome_cliente,
       c.cidade_cliente,
       d.saldo_deposito,
       e.valor_emprestimo
FROM cliente AS c,
     emprestimo AS e,
     deposito AS d
WHERE c.nome_cliente = d.nome_cliente -- verifica o nome do cliente entre as tres tabelas
  AND c.nome_cliente = e.nome_cliente
  AND c.cidade_cliente = 'Santa Luzia'
  AND d.saldo_deposito < 1000
  AND e.valor_emprestimo > 1000;

--07-Um gerente pretende criar uma lista de clientes que correm o risco
--de se individar de maneira irreversível. Para tanto ele formulou
--a seguinte pesquisa:
--Selecione os clientes da cidade de Santa Luzia com uma média de
--depósitos menor do que a média de empréstimos

SELECT c.nome_cliente,
       AVG(d.saldo_deposito),
       AVG(e.valor_emprestimo)
FROM cliente AS c,
     deposito AS d,
     emprestimo AS e
WHERE c.nome_cliente = d.nome_cliente
  AND c.nome_cliente = e.nome_cliente
  AND c.cidade_cliente = 'Santa Luzia'
GROUP BY c.nome_cliente
HAVING AVG(d.saldo_deposito) < AVG(e.valor_emprestimo);

--08-É preciso atualizar a informação do saldo do cliente na tabela conta.
--para este propósito devemos levar em conta o saldo dos depósitos menos os
--saldos de empréstimos. o cálculo final deve ser armazenado na tabela conta.

UPDATE conta
SET saldo_conta = 0;

SELECT nome_cliente, saldo_conta
FROM conta
ORDER BY saldo_conta DESC;

SELECT numero_conta, nome_agencia, nome_cliente, SUM(saldo_deposito)
FROM deposito
GROUP BY numero_conta, nome_agencia, nome_cliente;

--Primeiro os clientes que possuem deposito e emprestimos (ambos)

SELECT e.nome_cliente,
       e.nome_agencia,
       e.numero_conta,
       SUM(d.saldo_deposito) AS soma_dep,
       SUM(e.valor_emprestimo) as soma_emp,
       round(SUM(d.saldo_deposito)::numeric, 2) - round(SUM(e.valor_emprestimo)::numeric, 2) AS total
FROM emprestimo AS e,
     deposito AS d
WHERE e.nome_cliente = d.nome_cliente
  AND e.nome_agencia = d.nome_agencia
  AND e.numero_conta = d.numero_conta
GROUP BY e.nome_cliente, e.nome_agencia, e.numero_conta;

--Atualiza contas que possuem deposito e emprestimos (ambos)

UPDATE conta
SET saldo_conta = relatorio.total
FROM (SELECT e.nome_cliente,
             e.nome_agencia,
             e.numero_conta,
             SUM(d.saldo_deposito),
             SUM(e.valor_emprestimo),
             SUM(d.saldo_deposito) - SUM(e.valor_emprestimo) AS total
      FROM emprestimo AS e,
           deposito AS d
      WHERE e.nome_cliente = d.nome_cliente
        AND e.nome_agencia = d.nome_agencia
        AND e.numero_conta = d.numero_conta
      GROUP BY e.nome_cliente, e.nome_agencia, e.numero_conta) AS relatorio
WHERE conta.nome_cliente = relatorio.nome_cliente
  AND conta.nome_agencia = relatorio.nome_agencia
  AND conta.numero_conta = relatorio.numero_conta
