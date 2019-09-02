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
import { wsUrl } from '../../../api/api';
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
  lastCreatedPod,
  ...props
}) => {
  const [showModal, toggleShowModal] = useState(false);
  const [showTerminal, toggleShowTerminal] = useState(false);
  const [environmentId, setEnvironmentId] = useState(null);
  const [terminal, IdsetTerminal] = useState(null);
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
    setEnvironmentId(environmentId);
    getContainers(environmentId, serviceId);
  };

  const columns = tableColumns(
    ['lastUpdate', 'serviceName', 'environmentName', 'groupName', 'userEmail', 'deploymentMessage', 'status', 'actions'],
    ['Last Update', 'Service', 'Environment', 'Group', 'Initiated By', 'Deployment Message', 'Status', 'Actions'],
    [{ title: 'View logs', color: '#465BA4', onClick: handleViewLogsAction }, { title: 'Revert', color: '#BD656A' }],
  );

  const startLogsWebsocket = container => {
    const terminal = new Terminal();
    terminal.open(terminalRef.current);
    fit(terminal);
    const execUrl = `${wsUrl}/logs/pod/${lastCreatedPod}/container/${container}?environment=${environmentId}`;
    const webSocket = new WebSocket(execUrl);
    terminal.writeln('Opening web socket, wait a sec!');

    webSocket.onopen = e => {
      debugger;
      console.log(e.data);
      attach(terminal, webSocket, true, false);
    };
    webSocket.onerror = e => {
      debugger;
      console.log(e);
    };
  };

  if (!ongoingDeployments || !services || !environments) {
    return <Spinner />;
  }

  return (
    <div>
      {showModal && (
        <AppModal
          visible={true}
          toggleModal={toggleShowModal}
          onClose={() => toggleShowTerminal(false)}
          title="View Logs"
          width={1000}
        >
          {containers ? (
            containers.map((container, index) => (
              <div key={index} className={`${showTerminal ? 'modal-content-term' : ''}`}>
                {!showTerminal && (
                  <AppButton
                    key={index}
                    type="primary"
                    isLoading={isLoading}
                    label={container}
                    className={'modal-btn'}
                    onClick={async () => {
                      await toggleShowTerminal(true);
                      startLogsWebsocket(container);
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
      )}
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
