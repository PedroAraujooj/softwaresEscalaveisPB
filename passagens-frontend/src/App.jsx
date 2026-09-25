import { useEffect, useState } from 'react';

const PASSAGENS_API_URL = import.meta.env.VITE_PASSAGENS_API_URL || 'http://localhost:8081';
const PASSAGEIROS_API_URL = import.meta.env.VITE_PASSAGEIROS_API_URL || 'http://localhost:8082';

const formPassagemInicial = {
  passageiroId: '',
  assento: '',
  origem: '',
  destino: '',
  data: '',
  status: ''
};

const formPassageiroInicial = {
  nome: '',
  cpf: '',
  email: '',
  telefone: ''
};

function App() {
  const [passagens, setPassagens] = useState([]);
  const [passageiros, setPassageiros] = useState([]);
  const [formPassagem, setFormPassagem] = useState(formPassagemInicial);
  const [formPassageiro, setFormPassageiro] = useState(formPassageiroInicial);
  const [editandoPassagemId, setEditandoPassagemId] = useState(null);
  const [editandoPassageiroId, setEditandoPassageiroId] = useState(null);
  const [destinoBusca, setDestinoBusca] = useState('');
  const [nomeBusca, setNomeBusca] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [erro, setErro] = useState('');

  async function carregarPassagens() {
    try {
      setErro('');
      const resposta = await fetch(`${PASSAGENS_API_URL}/passagens`);

      if (!resposta.ok) {
        throw new Error('Erro ao listar passagens');
      }

      const dados = await resposta.json();
      setPassagens(dados);
    } catch (error) {
      setErro(error.message);
    }
  }

  async function carregarPassageiros() {
    try {
      setErro('');
      const resposta = await fetch(`${PASSAGEIROS_API_URL}/passageiros`);

      if (!resposta.ok) {
        throw new Error('Erro ao listar participantes');
      }

      const dados = await resposta.json();
      setPassageiros(dados);
    } catch (error) {
      setErro(error.message);
    }
  }

  useEffect(() => {
    carregarPassageiros();
    carregarPassagens();
  }, []);

  function atualizarCampoPassagem(evento) {
    const { name, value } = evento.target;
    setFormPassagem({ ...formPassagem, [name]: value });
  }

  function atualizarCampoPassageiro(evento) {
    const { name, value } = evento.target;
    setFormPassageiro({ ...formPassageiro, [name]: value });
  }

  async function salvarPassagem(evento) {
    evento.preventDefault();

    const passagem = {
      ...formPassagem,
      passageiroId: Number(formPassagem.passageiroId),
      assento: Number(formPassagem.assento)
    };

    const url = editandoPassagemId
      ? `${PASSAGENS_API_URL}/passagens/${editandoPassagemId}`
      : `${PASSAGENS_API_URL}/passagens`;

    const metodo = editandoPassagemId ? 'PUT' : 'POST';

    try {
      setErro('');
      setMensagem('');

      const resposta = await fetch(url, {
        method: metodo,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(passagem)
      });

      if (!resposta.ok) {
        const problema = await resposta.json().catch(() => ({}));
        throw new Error(problema.message || 'Erro ao salvar passagem');
      }

      setFormPassagem(formPassagemInicial);
      setEditandoPassagemId(null);
      setMensagem(editandoPassagemId ? 'Passagem atualizada com sucesso!' : 'Passagem criada com sucesso!');
      carregarPassagens();
    } catch (error) {
      setErro(error.message);
    }
  }

  function editarPassagem(passagem) {
    setFormPassagem({
      passageiroId: passagem.passageiroId || '',
      assento: passagem.assento || '',
      origem: passagem.origem || '',
      destino: passagem.destino || '',
      data: passagem.data || '',
      status: passagem.status || ''
    });

    setEditandoPassagemId(passagem.id);
    setMensagem('Editando passagem ID ' + passagem.id);
  }

  function cancelarEdicaoPassagem() {
    setFormPassagem(formPassagemInicial);
    setEditandoPassagemId(null);
    setMensagem('');
  }

  async function deletarPassagem(id) {
    const confirmou = window.confirm('Deseja realmente deletar esta passagem?');

    if (!confirmou) {
      return;
    }

    try {
      setErro('');
      setMensagem('');

      const resposta = await fetch(`${PASSAGENS_API_URL}/passagens/${id}`, {
        method: 'DELETE'
      });

      if (!resposta.ok) {
        throw new Error('Erro ao deletar passagem');
      }

      setMensagem('Passagem deletada com sucesso!');
      carregarPassagens();
    } catch (error) {
      setErro(error.message);
    }
  }

  async function buscarPorDestino(evento) {
    evento.preventDefault();

    if (!destinoBusca.trim()) {
      carregarPassagens();
      return;
    }

    try {
      setErro('');
      setMensagem('');

      const resposta = await fetch(`${PASSAGENS_API_URL}/passagens/busca?destino=${encodeURIComponent(destinoBusca)}`);

      if (!resposta.ok) {
        throw new Error('Erro ao buscar por destino');
      }

      const dados = await resposta.json();
      setPassagens(dados);
    } catch (error) {
      setErro(error.message);
    }
  }

  async function salvarPassageiro(evento) {
    evento.preventDefault();

    const url = editandoPassageiroId
      ? `${PASSAGEIROS_API_URL}/passageiros/${editandoPassageiroId}`
      : `${PASSAGEIROS_API_URL}/passageiros`;

    const metodo = editandoPassageiroId ? 'PUT' : 'POST';

    try {
      setErro('');
      setMensagem('');

      const resposta = await fetch(url, {
        method: metodo,
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(formPassageiro)
      });

      if (!resposta.ok) {
        throw new Error('Erro ao salvar participante');
      }

      setFormPassageiro(formPassageiroInicial);
      setEditandoPassageiroId(null);
      setMensagem(editandoPassageiroId ? 'Participante atualizado com sucesso!' : 'Participante criado com sucesso!');
      carregarPassageiros();
      carregarPassagens();
    } catch (error) {
      setErro(error.message);
    }
  }

  function editarPassageiro(passageiro) {
    setFormPassageiro({
      nome: passageiro.nome || '',
      cpf: passageiro.cpf || '',
      email: passageiro.email || '',
      telefone: passageiro.telefone || ''
    });

    setEditandoPassageiroId(passageiro.id);
    setMensagem('Editando participante ID ' + passageiro.id);
  }

  function cancelarEdicaoPassageiro() {
    setFormPassageiro(formPassageiroInicial);
    setEditandoPassageiroId(null);
    setMensagem('');
  }

  async function deletarPassageiro(id) {
    const confirmou = window.confirm('Deseja realmente deletar este participante?');

    if (!confirmou) {
      return;
    }

    try {
      setErro('');
      setMensagem('');

      const resposta = await fetch(`${PASSAGEIROS_API_URL}/passageiros/${id}`, {
        method: 'DELETE'
      });

      if (!resposta.ok) {
        throw new Error('Erro ao deletar participante');
      }

      setMensagem('Participante deletado com sucesso!');
      carregarPassageiros();
      carregarPassagens();
    } catch (error) {
      setErro(error.message);
    }
  }

  async function buscarPorNome(evento) {
    evento.preventDefault();

    if (!nomeBusca.trim()) {
      carregarPassageiros();
      return;
    }

    try {
      setErro('');
      setMensagem('');

      const resposta = await fetch(`${PASSAGEIROS_API_URL}/passageiros/busca?nome=${encodeURIComponent(nomeBusca)}`);

      if (!resposta.ok) {
        throw new Error('Erro ao buscar por nome');
      }

      const dados = await resposta.json();
      setPassageiros(dados);
    } catch (error) {
      setErro(error.message);
    }
  }

  function nomeDoPassageiro(passagem) {
    if (passagem.passageiro?.nome) {
      return passagem.passageiro.nome;
    }

    const passageiro = passageiros.find((item) => item.id === passagem.passageiroId);
    return passageiro?.nome || `ID ${passagem.passageiroId}`;
  }

  return (
    <main className="container">
      <h1>Sistema de Passagens</h1>
      <p>Cadastros e alterações de participantes podem levar alguns instantes para aparecer nas passagens. Use “Listar todas” para atualizar.</p>

      {(mensagem || erro) && (
        <div className="avisos">
          {mensagem && <p className="mensagem">{mensagem}</p>}
          {erro && <p className="erro">{erro}</p>}
        </div>
      )}

      <section className="modulo">
        <div className="modulo-cabecalho">
          <h2>Participantes</h2>
          <span>{passageiros.length} cadastrados</span>
        </div>

        <div className="grade-cards">
          <section className="card">
            <h3>{editandoPassageiroId ? 'Editar participante' : 'Cadastrar participante'}</h3>

            <form onSubmit={salvarPassageiro} className="formulario">
              <input
                name="nome"
                placeholder="Nome"
                value={formPassageiro.nome}
                onChange={atualizarCampoPassageiro}
                required
              />

              <input
                name="cpf"
                placeholder="CPF"
                value={formPassageiro.cpf}
                onChange={atualizarCampoPassageiro}
                required
              />

              <input
                name="email"
                type="email"
                placeholder="E-mail"
                value={formPassageiro.email}
                onChange={atualizarCampoPassageiro}
                required
              />

              <input
                name="telefone"
                placeholder="Telefone"
                value={formPassageiro.telefone}
                onChange={atualizarCampoPassageiro}
                required
              />

              <div className="botoes">
                <button type="submit">{editandoPassageiroId ? 'Atualizar' : 'Cadastrar'}</button>
                {editandoPassageiroId && (
                  <button type="button" className="secundario" onClick={cancelarEdicaoPassageiro}>
                    Cancelar
                  </button>
                )}
              </div>
            </form>
          </section>

          <section className="card">
            <h3>Buscar participante</h3>

            <form onSubmit={buscarPorNome} className="busca">
              <input
                placeholder="Digite o nome"
                value={nomeBusca}
                onChange={(evento) => setNomeBusca(evento.target.value)}
              />
              <button type="submit">Buscar</button>
              <button type="button" className="secundario" onClick={carregarPassageiros}>
                Listar todos
              </button>
            </form>
          </section>
        </div>

        <section className="card tabela-card">
          <h3>Participantes cadastrados</h3>

          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Nome</th>
                <th>CPF</th>
                <th>E-mail</th>
                <th>Telefone</th>
                <th>Acoes</th>
              </tr>
            </thead>
            <tbody>
              {passageiros.length === 0 ? (
                <tr>
                  <td colSpan="6">Nenhum participante encontrado.</td>
                </tr>
              ) : (
                passageiros.map((passageiro) => (
                  <tr key={passageiro.id}>
                    <td>{passageiro.id}</td>
                    <td>{passageiro.nome}</td>
                    <td>{passageiro.cpf}</td>
                    <td>{passageiro.email}</td>
                    <td>{passageiro.telefone}</td>
                    <td>
                      <button onClick={() => editarPassageiro(passageiro)}>Editar</button>
                      <button className="perigo" onClick={() => deletarPassageiro(passageiro.id)}>
                        Deletar
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </section>
      </section>

      <section className="modulo">
        <div className="modulo-cabecalho">
          <h2>Passagens</h2>
          <span>{passagens.length} cadastradas</span>
        </div>

        <div className="grade-cards">
          <section className="card">
            <h3>{editandoPassagemId ? 'Editar passagem' : 'Cadastrar passagem'}</h3>

            <form onSubmit={salvarPassagem} className="formulario">
              <select
                name="passageiroId"
                value={formPassagem.passageiroId}
                onChange={atualizarCampoPassagem}
                required
              >
                <option value="">Selecione um participante</option>
                {passageiros.map((passageiro) => (
                  <option key={passageiro.id} value={passageiro.id}>
                    {passageiro.nome} - CPF {passageiro.cpf}
                  </option>
                ))}
              </select>

              <input
                name="assento"
                type="number"
                placeholder="Assento"
                value={formPassagem.assento}
                onChange={atualizarCampoPassagem}
                required
              />

              <input
                name="origem"
                placeholder="Origem"
                value={formPassagem.origem}
                onChange={atualizarCampoPassagem}
                required
              />

              <input
                name="destino"
                placeholder="Destino"
                value={formPassagem.destino}
                onChange={atualizarCampoPassagem}
                required
              />

              <input
                name="data"
                type="date"
                value={formPassagem.data}
                onChange={atualizarCampoPassagem}
                required
              />

              <input
                name="status"
                placeholder="Status"
                value={formPassagem.status}
                onChange={atualizarCampoPassagem}
                required
              />

              <div className="botoes">
                <button type="submit">{editandoPassagemId ? 'Atualizar' : 'Cadastrar'}</button>
                {editandoPassagemId && (
                  <button type="button" className="secundario" onClick={cancelarEdicaoPassagem}>
                    Cancelar
                  </button>
                )}
              </div>
            </form>
          </section>

          <section className="card">
            <h3>Buscar passagem</h3>

            <form onSubmit={buscarPorDestino} className="busca">
              <input
                placeholder="Digite o destino"
                value={destinoBusca}
                onChange={(evento) => setDestinoBusca(evento.target.value)}
              />
              <button type="submit">Buscar</button>
              <button type="button" className="secundario" onClick={carregarPassagens}>
                Listar todas
              </button>
            </form>
          </section>
        </div>

        <section className="card tabela-card">
          <h3>Passagens cadastradas</h3>

          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Participante</th>
                <th>Assento</th>
                <th>Origem</th>
                <th>Destino</th>
                <th>Data</th>
                <th>Status</th>
                <th>Acoes</th>
              </tr>
            </thead>
            <tbody>
              {passagens.length === 0 ? (
                <tr>
                  <td colSpan="8">Nenhuma passagem encontrada.</td>
                </tr>
              ) : (
                passagens.map((passagem) => (
                  <tr key={passagem.id}>
                    <td>{passagem.id}</td>
                    <td>{nomeDoPassageiro(passagem)}</td>
                    <td>{passagem.assento}</td>
                    <td>{passagem.origem}</td>
                    <td>{passagem.destino}</td>
                    <td>{passagem.data}</td>
                    <td>{passagem.status}</td>
                    <td>
                      <button onClick={() => editarPassagem(passagem)}>Editar</button>
                      <button className="perigo" onClick={() => deletarPassagem(passagem.id)}>
                        Deletar
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </section>
      </section>
    </main>
  );
}

export default App;
