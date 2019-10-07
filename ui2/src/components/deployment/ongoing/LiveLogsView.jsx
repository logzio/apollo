import React, { useState } from 'react';
import { Terminal } from 'xterm';
import { fit } from 'xterm/lib/addons/fit/fit';
import { attach, detach } from 'xterm/lib/addons/attach/attach';
import { Spinner } from '../../../common/Spinner';
import { AppModal } from '../../../common/Modal';
import { AppButton } from '../../../common/Button';
import { getLiveLogsWebSocketUrl } from '../../../api/api';
import 'xterm/dist/xterm.css';

export const LiveLogsView = ({ environmentId, containers, lastCreatedPod, toggleShowModal }) => {
  const [showTerminal, toggleShowTerminal] = useState(false);
  const [logsTerminal, setLogsTerminal] = useState(null);
  const [logsWebsocket, setLogsWebsocket] = useState(null);
  const [terminalRef] = useState(React.createRef());

  const showLiveLogs = container => {
    const terminal = new Terminal();
    terminal.open(terminalRef.current);
    setLogsTerminal(terminal);
    fit(terminal);
    const webSocket = new WebSocket(getLiveLogsWebSocketUrl(lastCreatedPod, container, environmentId));
    terminal.writeln('Opening web socket, wait a sec!');

    webSocket.onopen = e => {
      console.log(e.data);
      setLogsWebsocket(webSocket);
      attach(terminal, webSocket, true, false);
    };

    webSocket.onerror = e => {
      console.log(e.data);
      detach(terminal, webSocket);
      webSocket.close();
    };
  };

  const closeLiveLogs = () => {
    logsTerminal && detach(logsTerminal, logsWebsocket);
    logsTerminal && logsTerminal.destroy();
    logsWebsocket && logsWebsocket.close();
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
      footer={[<AppButton label={'Close'} className={'modal-btn'} key="back" onClick={() => toggleShowModal(false)} />]}
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
