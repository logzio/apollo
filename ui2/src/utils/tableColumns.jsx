export const tableColumns = columns =>
  columns.map(column => ({
    dataIndex: column,
    title: column.charAt(0).toUpperCase() + column.substring(1),
  }));
