import React from 'react';
import { Modal } from 'antd';
import './Button.css';

export const AppModal = ({ children, toggleModal, okDisabled, ...props }) => {
  return (
    <Modal onCancel={() => toggleModal(false)} okButtonProps={{ disabled: okDisabled }} {...props}>
      {children}
    </Modal>
  );
};
