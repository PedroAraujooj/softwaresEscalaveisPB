import { useEffect, useState } from 'react';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

const formInicial = {
  passageiro: '',
  assento: '',
  origem: '',
  destino: '',
  data: '',
  status: ''
};

function App() {
  const [passagens, setPassagens] = useState([]);
  const [form, setForm] = useState(formInicial);
  const [editandoId, setEditandoId] = useState(null);
  const [destinoBusca, setDestinoBusca] = useState('');
  const [mensagem, setMensagem] = useState('');
  const [erro, setErro] = useState('');

  async function carregarPassagens() {
    try {
      setErro('');
      const resposta = await fetch(`${API_URL}/passagens`);

      if (!resposta.ok) {
        throw new Error('Erro ao listar passagens');
      }

      const dados = await resposta.json();
      setPassagens(dados);
    } catch (error) {
      setErro(error.message);
    }
  }

  useEffect(() => {
    carregarPassagens();
  }, []);

  function atualizarCampo(evento) {
    const { name, value } = evento.target;
    setForm({ ...form, [name]: value });
  }

  async function salvarPassagem(evento) {
    evento.preventDefault();

    const passagem = {
      ...form,
      assento: Number(form.assento)
    };

    const url = editandoId
      ? `${API_URL}/passagens/${editandoId}`
      : `${API_URL}/passagens`;

    const metodo = editandoId ? 'PUT' : 'POST';

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
        throw new Error('Erro ao salvar passagem');
      }

      setForm(formInicial);
      setEditandoId(null);
      setMensagem(editandoId ? 'Passagem atualizada com sucesso!' : 'Passagem criada com sucesso!');
      carregarPassagens();
    } catch (error) {
      setErro(error.message);
    }
  }

  function editarPassagem(passagem) {
    setForm({
      passageiro: passagem.passageiro || '',
      assento: passagem.assento || '',
      origem: passagem.origem || '',
      destino: passagem.destino || '',
      data: passagem.data || '',
      status: passagem.status || ''
    });

    setEditandoId(passagem.id);
    setMensagem('Editando passagem ID ' + passagem.id);
  }

  function cancelarEdicao() {
    setForm(formInicial);
    setEditandoId(null);
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

      const resposta = await fetch(`${API_URL}/passagens/${id}`, {
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

      const resposta = await fetch(`${API_URL}/passagens/busca?destino=${encodeURIComponent(destinoBusca)}`);

      if (!resposta.ok) {
        throw new Error('Erro ao buscar por destino');
      }

      const dados = await resposta.json();
      setPassagens(dados);
    } catch (error) {
      setErro(error.message);
    }
  }

  return (
    <main className="container">
      <h1>Sistema de Passagens</h1>

      <section className="card">
        <h2>{editandoId ? 'Editar passagem' : 'Cadastrar passagem'}</h2>

        <form onSubmit={salvarPassagem} className="formulario">
          <input
            name="passageiro"
            placeholder="Passageiro"
            value={form.passageiro}
            onChange={atualizarCampo}
            required
          />

          <input
            name="assento"
            type="number"
            placeholder="Assento"
            value={form.assento}
            onChange={atualizarCampo}
            required
          />

          <input
            name="origem"
            placeholder="Origem"
            value={form.origem}
            onChange={atualizarCampo}
            required
          />

          <input
            name="destino"
            placeholder="Destino"
            value={form.destino}
            onChange={atualizarCampo}
            required
          />

          <input
            name="data"
            type="date"
            value={form.data}
            onChange={atualizarCampo}
            required
          />

          <input
            name="status"
            placeholder="Status"
            value={form.status}
            onChange={atualizarCampo}
            required
          />

          <div className="botoes">
            <button type="submit">{editandoId ? 'Atualizar' : 'Cadastrar'}</button>
            {editandoId && (
              <button type="button" className="secundario" onClick={cancelarEdicao}>
                Cancelar
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="card">
        <h2>Buscar por destino</h2>

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

      {mensagem && <p className="mensagem">{mensagem}</p>}
      {erro && <p className="erro">{erro}</p>}

      <section className="card">
        <h2>Passagens cadastradas</h2>

        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Passageiro</th>
              <th>Assento</th>
              <th>Origem</th>
              <th>Destino</th>
              <th>Data</th>
              <th>Status</th>
              <th>Ações</th>
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
                  <td>{passagem.passageiro}</td>
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
    </main>
  );
}

export default App;
