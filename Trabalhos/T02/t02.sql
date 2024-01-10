-- Trabalho 02 de Banco de Dados 2
-- Autores: Igor Augusto e Heitor Guimarães

-- 9. Acrescentando 100.000 depósitos de R$ 1,00 (Um Real) na conta do
-- cliente 'Germano Luiz de Paula', na agência 'Pampulha', na conta 93134
DO
$do$
    BEGIN
        FOR num IN 1..100000
            LOOP
                INSERT INTO deposito
                VALUES (num, 93134, 'Pampulha', 'Germano Luiz de Paula',
                        1.00);
            END LOOP;
    END
$do$;
SELECT *
FROM deposito
WHERE nome_cliente = 'Germano Luiz de Paula';
------------------------------------------------------------------------------------------------------------------------
-- 11.1. Selecionando os nomes dos clientes e seus respectivos números de conta e
-- nome de agência que fizeram depósitos e empréstimos ao mesmo tempo.
SELECT nome_cliente, numero_conta, nome_agencia
FROM emprestimo
INTERSECT
SELECT nome_cliente, numero_conta, nome_agencia
FROM deposito;
------------------------------------------------------------------------------------------------------------------------
-- 11.2. Construa a consulta equivalente a este exemplo utilizando a cláusula JOIN
SELECT e.nome_cliente, e.numero_conta, e.nome_agencia
FROM emprestimo e
         INNER JOIN deposito d
                    ON e.nome_cliente = d.nome_cliente
                        AND e.numero_conta = d.numero_conta
                        AND e.nome_agencia = d.nome_agencia
GROUP BY e.nome_cliente, e.numero_conta, e.nome_agencia;
------------------------------------------------------------------------------------------------------------------------
-- 11.3. Construa a consulta equivalente a este exemplo utilizando a SELECT DISTINCT e sem o JOIN.
SELECT DISTINCT e.nome_cliente, e.numero_conta, e.nome_agencia
FROM emprestimo e,
     deposito d
WHERE e.nome_cliente = d.nome_cliente
  AND e.numero_conta = d.numero_conta
  AND e.nome_agencia = d.nome_agencia;
------------------------------------------------------------------------------------------------------------------------
-- 13.1. Implemente primeiro (por ser mais simples) uma função em PL/pgSQL para retornar
-- o relatório descrito no cenário 2 a partir desta consulta SQL;
-- select faixa_cliente(nome_cliente), nome_cliente from cliente;
------------------------------------------------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION faixa_cliente(cliente_nome varchar)
    RETURNS varchar AS
$$
DECLARE
    soma_depositos numeric;
    faixa          varchar;
BEGIN
    -- Calcula a soma dos depósitos para o cliente
    SELECT COALESCE(SUM(saldo_deposito), 0)
    INTO soma_depositos
    FROM deposito
    WHERE nome_cliente = cliente_nome;

    -- Determina a faixa do cliente com base na soma dos depósitos
    IF soma_depositos > 6000 THEN
        faixa := 'A';
    ELSIF soma_depositos > 4000 THEN
        faixa := 'B';
    ELSE
        faixa := 'C';
    END IF;

    RETURN faixa;
END;
$$ LANGUAGE plpgsql;
-- consulta para verificar
SELECT faixa_cliente(nome_cliente), nome_cliente
FROM cliente;
SELECT *
FROM deposito;

------------------------------------------------------------------------------------------------------------------------
-- 13.2. Implemente uma função em PL/pgSQL para retornar o relatório descrito no cenário 1:
-- select nome_cliente, contas_cliente(nome_cliente), cidade_cliente from cliente;
CREATE OR REPLACE FUNCTION contas_cliente(p_nome_cliente character varying)
    RETURNS character varying AS
$BODY$
DECLARE
    dados_agencia character varying;
    dados_conta   int;
    dados_cliente character varying;
    conta         character varying;
    cursor_relatorio CURSOR FOR
        SELECT nome_cliente, nome_agencia, numero_conta
        FROM conta
        WHERE nome_cliente = p_nome_cliente;
BEGIN
    OPEN cursor_relatorio;
    conta := '';
    LOOP
        FETCH cursor_relatorio INTO dados_cliente, dados_agencia, dados_conta;
        IF FOUND THEN
            conta := conta || dados_agencia || '-' || dados_conta || ', ';
        END IF;
        IF NOT FOUND THEN
            CLOSE cursor_relatorio;
            RETURN conta;
        END IF;
    END LOOP;
END;
$BODY$
    LANGUAGE plpgsql VOLATILE
                     COST 100;
------------------------------------------------------------------------------------------------------------------------
-- essa consulta (que é a requisitada) não exclui os campos nulos
SELECT nome_cliente, contas_cliente(nome_cliente), cidade_cliente
FROM cliente;
-- já essa oculta as linhas que possuem a coluna contas_cliente como null
SELECT nome_cliente, contas_cliente(nome_cliente), cidade_cliente
FROM cliente
WHERE contas_cliente(nome_cliente) IS NOT NULL
  AND contas_cliente(nome_cliente) <> '';
