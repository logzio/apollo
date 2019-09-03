import React, { useState } from 'react';
import { Terminal } from 'xterm';
import { fit } from 'xterm/lib/addons/fit/fit';
import { attach, detach } from 'xterm/lib/addons/attach/attach';
import { Spinner } from '../../../common/Spinner';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import { wsUrl } from '../../../api/api';
import 'xterm/dist/xterm.css';

export const LiveLogsView = ({ environmentId, containers, lastCreatedPod, toggleShowModal }) => {
  const [showTerminal, toggleShowTerminal] = useState(false);
  const [terminalTest, setTerminal] = useState(null);
  const [websocketTest, setWebsocket] = useState(null);
  const [terminalRef, setTerminalRef] = useState(React.createRef());

  const showLiveLogs = container => {
    const terminal = new Terminal();
    terminal.open(terminalRef.current);
    setTerminal(terminal);
    fit(terminal);
    const execUrl = `${wsUrl}/logs/pod/${lastCreatedPod}/container/${container}?environment=${environmentId}`;
    const webSocket = new WebSocket(execUrl);
    terminal.writeln('Opening web socket, wait a sec!');

    webSocket.onopen = e => {
      debugger;
      setWebsocket(webSocket);
      console.log(e.data);
      attach(terminalTest, webSocket, true, false);
    };

    webSocket.onerror = e => {
      debugger;
      console.log(e);
      // detach(terminalTest, websocket)
      // websocket.close();
    };
  };

  const closeLiveLogs = () => {
    // terminalTest && detach(terminalTest, websocket);
    terminalTest && terminalTest.destroy();
    websocketTest && websocketTest.close();
  };

  return (
    <AppModal
      visible={true}
      toggleModal={toggleShowModal}
      onClose={() => {
        toggleShowTerminal(false);
        closeLiveLogs();
      }}
      title={!showTerminal ? 'Select a container to view logs from' : 'Live logs from the latest created container'}
      width={1000}
    >
      {containers ? (
        containers.map((container, index) => (
          <div key={index} className={`${showTerminal ? 'modal-content-term' : ''}`}>
            {!showTerminal && (
              <AppButton
                key={index}
                type="primary"
                label={container}
                className={'modal-btn'}
                onClick={async () => {
                  await toggleShowTerminal(true);
                  showLiveLogs(container);
                }}
              />
            )}
            {showTerminal && <div ref={terminalRef} />}
          </div>
        ))
      ) : (
        <Spinner />
      )}
    </AppModal>
  );
};
