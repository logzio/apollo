import React from 'react';
import { Modal } from 'antd';
import './Button.css';

export const AppModal = ({ children, toggleModal, okDisabled, onClose, ...props }) => {
  return (
    <Modal
      onCancel={() => {
        toggleModal(false);
        onClose && onClose();
      }}
      okButtonProps={{ disabled: okDisabled }}
      width={1000}
      {...props}
    >
      {children}
    </Modal>
  );
};
