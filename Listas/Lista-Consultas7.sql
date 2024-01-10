--PRIMEIRO CONFERIMOS COMO ESTá A SITUACAO DAS CONTAS
SELECT *
FROM emprestimo
WHERE nome_agencia = 'PUC';

--EXECUTAMOS OUTRA CONFERENCIA PARA VER COMO SERãO OS RETORNOS DA PESQUISA
SELECT conta.*, update_valor_emprestimo1(numero_conta, nome_agencia, nome_cliente)
FROM conta
WHERE nome_agencia = 'PUC';

--A DEFINIÇÃO DA FUNÇÃO update_valor_emprestimo1
CREATE OR REPLACE FUNCTION update_valor_emprestimo1(p_numero_conta integer,
                                                    p_nome_agencia character varying,
                                                    p_nome_cliente character varying
)
    RETURNS float AS
$BODY$
DECLARE
    l_valor_emprestimo float;
    l_valor_juros      float;
    cursor_relatorio CURSOR FOR SELECT valor_emprestimo, juros_emprestimo
                                FROM emprestimo
                                WHERE nome_cliente = p_nome_cliente
                                  AND nome_agencia = p_nome_agencia
                                  AND numero_conta = p_numero_conta;
BEGIN
    OPEN cursor_relatorio;
    FETCH cursor_relatorio INTO l_valor_emprestimo, l_valor_juros;
    IF FOUND THEN
        l_valor_emprestimo = l_valor_emprestimo * (1 + (l_valor_juros) / 100); -- 100% + X% de juros
        UPDATE emprestimo
        SET valor_emprestimo = l_valor_emprestimo
        WHERE nome_cliente = p_nome_cliente
          AND nome_agencia = p_nome_agencia
          AND numero_conta = p_numero_conta;
    END IF;
    CLOSE cursor_relatorio;
    RETURN l_valor_emprestimo;
END
$BODY$
    LANGUAGE plpgsql VOLATILE
                     COST 100;
ALTER FUNCTION update_valor_emprestimo1(integer, character varying, character varying)
    OWNER TO aluno;

--Lista as quantidades de emprestimos realizados por cada cliente
SELECT nome_cliente, COUNT(1)
FROM emprestimo
GROUP BY nome_cliente
ORDER BY COUNT(1) DESC;

--Lista apenas um deles
SELECT numero_emprestimo,
       nome_cliente,
       numero_conta,
       nome_agencia,
       ROUND(valor_emprestimo::numeric, 2) AS valor_emprestimo,
       juros_emprestimo,
       data_emprestimo
FROM emprestimo
WHERE nome_cliente = 'Reinaldo Pereira da Silva';

--Agora atualiza apenas os valores de empréstimos desta pessoa
SELECT update_valor_emprestimo2('Reinaldo Pereira da Silva');

--A DEFINIÇÃO DO PROCEDIMENTO update_valor_emprestimo2
CREATE OR REPLACE FUNCTION update_valor_emprestimo2(p_nome_cliente character varying)
    RETURNS void AS
$BODY$
DECLARE
    l_valor_emprestimo  float;
    l_valor_juros       float;
    l_numero_emprestimo integer;
    cursor_relatorio CURSOR FOR SELECT valor_emprestimo, juros_emprestimo, numero_emprestimo
                                FROM emprestimo
                                WHERE nome_cliente = p_nome_cliente;
BEGIN
    OPEN cursor_relatorio;
    LOOP
        FETCH cursor_relatorio INTO l_valor_emprestimo, l_valor_juros, l_numero_emprestimo;
        IF FOUND THEN
            l_valor_emprestimo = l_valor_emprestimo * (1 + (l_valor_juros) / 100); -- 100% + X% de juros
            UPDATE emprestimo
            SET valor_emprestimo = l_valor_emprestimo
            WHERE numero_emprestimo = l_numero_emprestimo;
        END IF;
        IF NOT FOUND THEN
            EXIT;
        END IF;
    END LOOP;
    CLOSE cursor_relatorio;
    RETURN;
END
$BODY$
    LANGUAGE plpgsql VOLATILE
                     COST 100;
ALTER FUNCTION update_valor_emprestimo2(character varying)
    OWNER TO aluno;
