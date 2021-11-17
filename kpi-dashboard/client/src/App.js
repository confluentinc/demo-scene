import logo from './logo.svg';
import './App.css';

import {useState, useEffect} from 'react';

const WS_URL = "ws://localhost:8080/ws";
const ws = new WebSocket(WS_URL);

function App() {
  const [webSocket, _] = useState(ws);
  const [total, setTotal] = useState(null);

  useEffect(() => {
    webSocket.onmessage = (event) => {
      const payload = JSON.parse(event.data);
      const total = payload['TOTAL'];
      const formatted = total.toLocaleString(undefined, {minimumFractionDigits: 2});
      setTotal(formatted);
    };
  }, [webSocket]);

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <p>
          {total || "...waiting..."}
        </p>
      </header>
    </div>
  );
}

export default App;
