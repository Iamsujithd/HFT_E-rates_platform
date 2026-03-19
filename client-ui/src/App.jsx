import React, { useState, useEffect, useRef, useCallback } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { Activity, ArrowDownRight, ArrowUpRight, DollarSign, Send } from 'lucide-react';

const GATEWAY_WS_URL = `http://${window.location.hostname}:8081/ws`;
const CLIENT_ID = 'client-' + Math.floor(Math.random() * 100000);

export default function App() {
  const [rates, setRates] = useState({});
  const [flashStates, setFlashStates] = useState({});
  const [tradeLogs, setTradeLogs] = useState([]);
  const [selectedInstrument, setSelectedInstrument] = useState('US_10Y');
  const [amount, setAmount] = useState(1000000);
  const [side, setSide] = useState('BUY');
  
  const stompClient = useRef(null);

  useEffect(() => {
    // Initialize STOMP client
    const client = new Client({
      webSocketFactory: () => new SockJS(GATEWAY_WS_URL),
      debug: (str) => console.log('STOMP: ' + str),
      reconnectDelay: 5000,
    });

    client.onConnect = () => {
      console.log('Connected to WebSocket Gateway');
      
      // Subscribe to rates
      client.subscribe('/topic/rates', (message) => {
        if (message.body) {
          const tick = JSON.parse(message.body);
          handleRateUpdate(tick);
        }
      });

      // Subscribe to personal trade responses
      client.subscribe(`/topic/trades/${CLIENT_ID}`, (message) => {
        if (message.body) {
          const response = JSON.parse(message.body);
          handleTradeResponse(response);
        }
      });
    };

    client.activate();
    stompClient.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const handleRateUpdate = useCallback((tick) => {
    setRates(prev => {
      const prevUpdate = prev[tick.instrumentId];
      let direction = null;
      if (prevUpdate) {
        if (tick.bidPrice > prevUpdate.bidPrice) direction = 'up';
        if (tick.bidPrice < prevUpdate.bidPrice) direction = 'down';
      }
      
      if (direction) {
        setFlashStates(fs => ({ ...fs, [tick.instrumentId]: direction }));
        setTimeout(() => setFlashStates(fs => ({ ...fs, [tick.instrumentId]: null })), 500);
      }
      
      return { ...prev, [tick.instrumentId]: tick };
    });
  }, []);

  const handleTradeResponse = useCallback((response) => {
    setTradeLogs(prev => [response, ...prev].slice(0, 50));
  }, []);

  const executeTrade = (e) => {
    e.preventDefault();
    if (!stompClient.current || !stompClient.current.connected) {
      alert("WebSocket not connected");
      return;
    }
    
    const rate = rates[selectedInstrument];
    if (!rate) return;

    const requestedPrice = side === 'BUY' ? rate.askPrice : rate.bidPrice;

    const tradeRequest = {
      clientId: CLIENT_ID,
      instrumentId: selectedInstrument,
      side: side,
      amount: parseFloat(amount),
      requestedPrice: requestedPrice,
      requestTimestamp: new Date().toISOString()
    };

    stompClient.current.publish({
      destination: '/app/trade',
      body: JSON.stringify(tradeRequest)
    });
    
    setTradeLogs(prev => [{
      tradeId: 'PENDING...',
      status: 'PENDING',
      instrumentId: selectedInstrument,
      side,
      requestedPrice
    }, ...prev]);
  };

  return (
    <div className="app-container">
      <header className="header">
        <h1><Activity strokeWidth={2.5} className="text-blue-500" /> HFT Platform / Premium Web Rates</h1>
        <div className="text-sm opacity-70 flex items-center gap-2">
          <span className="w-2 h-2 rounded-full bg-green-500 inline-block animate-pulse"></span>
          Live Stream Active
        </div>
      </header>

      <div className="dashboard-grid">
        <div className="rates-panel">
          <h2 className="text-xl mb-4 font-semibold opacity-90">Live Market Rates</h2>
          <div className="rates-grid">
            {Object.values(rates).map(rate => {
              const flash = flashStates[rate.instrumentId];
              const flashClass = flash === 'up' ? 'flash-up-anim' : flash === 'down' ? 'flash-down-anim' : '';
              const isSelected = selectedInstrument === rate.instrumentId;
              
              return (
                <div 
                  key={rate.instrumentId} 
                  className={`glass-panel rate-card ${flashClass} ${isSelected ? 'selected' : ''}`}
                  onClick={() => setSelectedInstrument(rate.instrumentId)}
                >
                  <div className="instrument-id">{rate.instrumentId.replace('_', ' ')}</div>
                  <div className="flex gap-4">
                    <div className="flex-1">
                      <div className="price-row">
                        <span className="price-label">BID</span>
                        {flash === 'down' && <ArrowDownRight size={16} className="text-red-500 mt-1" />}
                      </div>
                      <div className={`price-value ${flash === 'down' ? 'price-down' : ''}`}>
                        {rate.bidPrice?.toFixed(4)}
                      </div>
                    </div>
                    <div className="w-px bg-white/10"></div>
                    <div className="flex-1">
                      <div className="price-row">
                        <span className="price-label">ASK</span>
                        {flash === 'up' && <ArrowUpRight size={16} className="text-green-500 mt-1" />}
                      </div>
                      <div className={`price-value ${flash === 'up' ? 'price-up' : ''}`}>
                        {rate.askPrice?.toFixed(4)}
                      </div>
                    </div>
                  </div>
                </div>
              );
            })}
            {Object.keys(rates).length === 0 && (
              <div className="glass-panel opacity-50 flex items-center justify-center p-8">
                Waiting for streaming data...
              </div>
            )}
          </div>
        </div>

        <div className="flex flex-col gap-6">
          <div className="glass-panel execution-panel flex-1">
            <h2 className="text-xl mb-4 font-semibold opacity-90 flex items-center gap-2">
              <DollarSign size={20} /> Execution
            </h2>
            <form onSubmit={executeTrade}>
              <label className="text-sm opacity-80 block mt-2">Instrument</label>
              <select 
                value={selectedInstrument} 
                onChange={e => setSelectedInstrument(e.target.value)}
              >
                <option value="US_10Y">US 10Y</option>
                <option value="US_5Y">US 5Y</option>
                <option value="US_2Y">US 2Y</option>
                <option value="EUR_BUND">EUR BUND</option>
              </select>

              <label className="text-sm opacity-80 block">Side</label>
              <div className="flex gap-2 mb-4 mt-2">
                <button type="button" 
                  className={`flex-1 p-2 rounded-lg font-bold ${side === 'BUY' ? 'bg-green-600 text-white' : 'bg-white/10 text-white'}`}
                  onClick={() => setSide('BUY')}>BUY</button>
                <button type="button" 
                  className={`flex-1 p-2 rounded-lg font-bold ${side === 'SELL' ? 'bg-red-600 text-white' : 'bg-white/10 text-white'}`}
                  onClick={() => setSide('SELL')}>SELL</button>
              </div>

              <label className="text-sm opacity-80 block">Amount</label>
              <input 
                type="number" 
                value={amount} 
                onChange={e => setAmount(e.target.value)} 
                min="1000" step="1000"
              />

              <button type="submit" className="button-primary flex items-center justify-center gap-2">
                <Send size={18} /> Execute Trade
              </button>
            </form>
          </div>

          <div className="glass-panel flex-1">
            <h2 className="text-xl mb-4 font-semibold opacity-90">Trade Blotter</h2>
            <div className="trade-log">
              {tradeLogs.map((log, i) => (
                <div key={i} className="log-entry">
                  <div>
                    <div className="font-bold">{log.side || ''} {log.instrumentId || ''}</div>
                    <div className="text-xs opacity-70">Price: {log.executedPrice || log.requestedPrice}</div>
                  </div>
                  <div className={`status-badge ${log.status === 'ACCEPTED' ? 'status-accepted' : log.status === 'REJECTED' ? 'status-rejected' : 'bg-yellow-500/20 text-yellow-500'}`}>
                    {log.status}
                  </div>
                </div>
              ))}
              {tradeLogs.length === 0 && (
                <div className="text-sm opacity-50 text-center py-4">No trades yet</div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
