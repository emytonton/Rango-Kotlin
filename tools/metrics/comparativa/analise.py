"""Análise da medição comparativa KMP x React Native.

Lê os CSVs de docs/medicoes/comparativa dos dois repositórios e, para cada métrica, calcula
n, mediana, quartis, intervalo interquartil, teste U de Mann-Whitney bilateral e delta de Cliff.
O p-valor é exato (distribuição de U por contagem) quando não há empates e n <= 30 em cada grupo;
nos demais casos usa a aproximação normal com correção de empates e de continuidade.
Sem dependências externas. Uso: py -3 analise.py <repo_kmp> <repo_rn> [saida.csv]
"""
import csv
import math
import statistics
import sys
from pathlib import Path


def ler(repo, arquivo):
    caminho = Path(repo) / "docs" / "medicoes" / "comparativa" / arquivo
    if not caminho.exists():
        return []
    with open(caminho, encoding="utf-8") as f:
        return list(csv.DictReader(f))


def quartis(valores):
    q = statistics.quantiles(valores, n=4, method="inclusive")
    return q[0], q[2]


def postos(valores):
    ordenados = sorted(range(len(valores)), key=lambda i: valores[i])
    r = [0.0] * len(valores)
    i = 0
    while i < len(ordenados):
        j = i
        while j + 1 < len(ordenados) and valores[ordenados[j + 1]] == valores[ordenados[i]]:
            j += 1
        media = (i + j) / 2 + 1
        for k in range(i, j + 1):
            r[ordenados[k]] = media
        i = j + 1
    return r


def distribuicao_u(n1, n2):
    # f[i][j][u]: número de arranjos com i de um grupo e j do outro e estatística u
    memo = {}

    def contar(i, j):
        if (i, j) in memo:
            return memo[(i, j)]
        if i == 0 or j == 0:
            d = {0: 1}
        else:
            d = {}
            for u, c in contar(i - 1, j).items():
                d[u + j] = d.get(u + j, 0) + c
            for u, c in contar(i, j - 1).items():
                d[u] = d.get(u, 0) + c
        memo[(i, j)] = d
        return d

    return contar(n1, n2)


def mann_whitney(a, b):
    n1, n2 = len(a), len(b)
    juntos = a + b
    r = postos(juntos)
    r1 = sum(r[:n1])
    u1 = r1 - n1 * (n1 + 1) / 2
    u2 = n1 * n2 - u1
    u = min(u1, u2)
    tem_empate = len(set(juntos)) < len(juntos)
    if not tem_empate and n1 <= 30 and n2 <= 30:
        dist = distribuicao_u(n1, n2)
        total = math.comb(n1 + n2, n1)
        cauda = sum(c for k, c in dist.items() if k <= u)
        return u1, min(1.0, 2 * cauda / total), "exato"
    n = n1 + n2
    contagens = {}
    for v in juntos:
        contagens[v] = contagens.get(v, 0) + 1
    correcao = sum(t ** 3 - t for t in contagens.values()) / (n * (n - 1))
    sigma = math.sqrt(n1 * n2 / 12 * ((n + 1) - correcao))
    if sigma == 0:
        return u1, 1.0, "normal"
    z = (abs(u1 - n1 * n2 / 2) - 0.5) / sigma
    p = math.erfc(z / math.sqrt(2))
    return u1, min(1.0, p), "normal"


def cliff(a, b):
    maior = sum(1 for x in a for y in b if x > y)
    menor = sum(1 for x in a for y in b if x < y)
    return (maior - menor) / (len(a) * len(b))


def magnitude(d):
    d = abs(d)
    if d < 0.147:
        return "desprezível"
    if d < 0.33:
        return "pequeno"
    if d < 0.474:
        return "médio"
    return "grande"


def series(kmp, rn):
    s = {}

    def add(nome, unidade, fw, valor):
        try:
            v = float(valor)
        except (TypeError, ValueError):
            return
        s.setdefault((nome, unidade), {"kmp": [], "react-native": []})[fw].append(v)

    for repo in (kmp, rn):
        for l in ler(repo, "inicializacao-frio.csv"):
            add("inicializacao_frio_total_time", "ms", l["framework"], l["total_time_ms"])
        for l in ler(repo, "memoria-cpu.csv"):
            if l["roteiro"] != "ok":
                continue
            add("memoria_pss", "KB", l["framework"], l["pss_kb"])
            add("cpu_roteiro", "ms", l["framework"], l["cpu_ms"])
            add("duracao_roteiro", "ms", l["framework"], l["duracao_roteiro_ms"])
        for l in ler(repo, "tempo-build.csv"):
            add("build_debug_" + l["cenario"], "s", l["framework"], l["tempo_s"])
    return s


def main():
    kmp, rn = sys.argv[1], sys.argv[2]
    saida = sys.argv[3] if len(sys.argv) > 3 else None
    linhas = []
    for (nome, unidade), v in series(kmp, rn).items():
        a, b = v["kmp"], v["react-native"]
        if len(a) < 2 or len(b) < 2:
            continue
        qa1, qa3 = quartis(a)
        qb1, qb3 = quartis(b)
        u, p, metodo = mann_whitney(a, b)
        d = cliff(a, b)
        linhas.append({
            "metrica": nome, "unidade": unidade,
            "n_kmp": len(a), "mediana_kmp": statistics.median(a), "q1_kmp": qa1, "q3_kmp": qa3, "iqr_kmp": qa3 - qa1,
            "n_rn": len(b), "mediana_rn": statistics.median(b), "q1_rn": qb1, "q3_rn": qb3, "iqr_rn": qb3 - qb1,
            "u_mann_whitney": u, "p_valor": p, "metodo_p": metodo,
            "delta_cliff": d, "magnitude": magnitude(d),
        })
    campos = list(linhas[0].keys()) if linhas else []
    escritor = csv.DictWriter(open(saida, "w", newline="", encoding="utf-8") if saida else sys.stdout, fieldnames=campos)
    escritor.writeheader()
    for l in linhas:
        escritor.writerow({k: (f"{x:.4g}" if isinstance(x, float) else x) for k, x in l.items()})


if __name__ == "__main__":
    main()
