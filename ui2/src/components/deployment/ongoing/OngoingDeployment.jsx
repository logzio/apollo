import React, { useEffect, useState } from 'react';
import { connect } from 'react-redux';
import { Terminal } from 'xterm';
import { fit } from 'xterm/lib/addons/fit/fit';
import { attach } from 'xterm/lib/addons/attach/attach';
import moment from 'moment';
import {
  getEnvironments,
  getOngoingDeployments,
  getServices,
  getContainers,
} from '../../../store/actions/deploymentActions';
import { Spinner } from '../../../common/Spinner';
import { AppTable } from '../../../common/Table';
import { AppModal } from '../../../common/Modal';
import { tableColumns } from '../../../utils/tableColumns';
import { AppButton } from '../../../common/Button';
import 'xterm/dist/xterm.css';
import './OngoingDeployment.css';

const OngoingDeploymentComponent = ({
  getOngoingDeployments,
  ongoingDeployments,
  getServices,
  services,
  getEnvironments,
  environments,
  handleBreadcrumbs,
  getContainers,
  containers,
  isLoading,
  location,
  ...props
}) => {
  const [showModal, toggleShowModal] = useState(false);
  const [terminal, setTerminal] = useState(null);
  const [websocket, setWebsocket] = useState(null);
  const [terminalRef, setTerminalRef] = useState(React.createRef());
  useEffect(() => {
    handleBreadcrumbs('ongoing');
    getServices();
    getEnvironments();
    getOngoingDeployments();
  }, []);

  const findNameById = (itemId, itemsList) => {
    let itemName = null;
    itemsList &&
      itemsList.map(({ id, name }) => {
        if (id === itemId) {
          itemName = name;
        }
      });
    return itemName;
  };

  const formattedData =
    ongoingDeployments &&
    ongoingDeployments.map(({ id, lastUpdate, serviceId, environmentId, groupName, ...dataItem }) => {
      return {
        ...dataItem,
        id: id,
        key: id.toString(),
        lastUpdate: moment(lastUpdate).format('DD/MM/YY, h:mm:ss'),
        serviceId: serviceId,
        serviceName: findNameById(serviceId, services),
        environmentId: environmentId,
        environmentName: findNameById(environmentId, environments),
        groupName: groupName ? groupName : 'Non',
      };
    });

  const handleViewLogsAction = (environmentId, serviceId) => {
    toggleShowModal(true);
    getContainers(environmentId, serviceId);
  };

  const columns = tableColumns(
    ['lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'deploymentMessage', 'status', 'actions'],
    ['Last Update', 'Service', 'Environment', 'Group', 'Initiated By', 'Deployment Message', 'Status', 'Actions'],
    [{ title: 'View logs', color: '#465BA4', onClick: handleViewLogsAction }, { title: 'Revert', color: '#BD656A' }],
  );

  const startLogsWebsocket = () => {
    const terminal = new Terminal({
      convertEol: true,
      fontFamily: `'Fira Mono', monospace`,
      fontSize: 15,
      rendererType: 'dom', // default is canvas
    });
    terminal.setOption('theme', {
      background: '#030405',
      foreground: '#fcfcfc',
    });

    // fit(terminal);
    // debugger;
    // const execUrl = location.protocol === "https:" ? `wss://${location.host}/ws/` : `ws://${location.host}/ws/`,
    // const execUrl = `ws://${location.host}/ws/exec/pod/${podName}/container/${containerName}?environment=${environment}&service=${service}`,
    const execUrl = `ws://${document.location.host}/ws/exec/pod/kibana-6-5467c75db7-6zq47/container/kibana-6-node?environment=2`;
    const webSocket = new WebSocket(execUrl);
    // const test = webSocket.readyState;
    terminal.writeln('Opening web socket, wait a sec!');

    webSocket.onopen = (webSocket, e) => {
      debugger;
      console.log(e.data);
      attach(terminal, webSocket, true, false);
    };
    webSocket.onmessage = e => {
      debugger;
      console.log(e.data);
      attach(terminal, webSocket, true, false);
    };
    webSocket.onerror = e => {
      debugger;
      console.log('error');
    };
    terminal.open(terminalRef.current);
    attach(terminal, webSocket, true, false);
  };

  if (!ongoingDeployments || !services || !environments) {
    return <Spinner />;
  }

  return (
    <div>
      <button onClick={() => startLogsWebsocket()}>test</button>
      <AppModal visible={showModal} toggleModal={toggleShowModal} title="View Logs">
        {containers ? (
          containers.map((container, index) => (
            <AppButton
              key={index}
              type="primary"
              isLoading={isLoading}
              label={container}
              className="modal-btn"
              onClick={() => startLogsWebsocket()}
            />
          ))
        ) : (
          <Spinner />
        )}
      </AppModal>
      <div ref={terminalRef} className={'test'} />
      <AppTable
        columns={columns}
        data={formattedData}
        scroll={{ y: 650 }} //600
        showSearch={true}
        searchColumns={['lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'status']}
        showSelection={false}
        emptyMsg={"There aren't ongoing deployments"}
        rowClassName={({ groupName }) => (groupName ? 'hide-row-expand-icon' : '')} //TODO
        expandableColumn={3}
        expandIconAsCell={false}
        handleViewLogsAction={handleViewLogsAction}
      />
    </div>
  );
};

const mapStateToProps = ({
  deploy: { isLoading, ongoingDeployments, services, environments, lastCreatedPod, lastCreatedGroupPod, containers },
}) => ({
  isLoading,
  ongoingDeployments,
  services,
  environments,
  lastCreatedPod,
  lastCreatedGroupPod,
  containers,
});

export const OngoingDeployment = connect(
  mapStateToProps,
  {
    getOngoingDeployments,
    getServices,
    getEnvironments,
    getContainers,
  },
)(OngoingDeploymentComponent);
