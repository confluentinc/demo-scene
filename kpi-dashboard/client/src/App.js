import logo from './logo.svg';
import './App.css';
import { useState, useEffect } from 'react';

const WS_URL = "ws://localhost:8080/websocket";

function App() {
  const [websocket, _] = useState(new WebSocket(WS_URL));
  const [total, setTotal] = useState(null);

  useEffect(() => {
    websocket.onmessage = (event) => {
      const payload = JSON.parse(event.data);

      const formatted = payload["TOTAL"].toLocaleString(
        undefined,
        {
          minimumFractionDigits: 2,
          style: 'currency',
          currency: 'USD'
        }
      );

      setTotal(formatted);
    };
  }, [websocket]);

  return (
    <div className="App">
      <header className="App-header">
        <img src={logo} className="App-logo" alt="logo" />
        <h2>{total || "...waiting..."}</h2>
      </header>
    </div>
  );
}

export default App;
