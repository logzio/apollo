import React from 'react';
import { Modal } from 'antd';
import './Button.css';

export const AppModal = ({ title, children, visible, toggleModal, customFooter, onOk, okDisabled }) => {
  return (
    <Modal
      title={title}
      visible={visible}
      onOk={onOk}
      onCancel={() => toggleModal(false)}
      footer={customFooter}
      okButtonProps={{ disabled: okDisabled }}
    >
      {children}
    </Modal>
  );
};

// onOk={({target: {value}}) => {
//     toggleModal(false);
//     onOk(value);
// }}
