import React, { useState, useEffect } from 'react';

export function useUserService(getServices, getSelectedServices) {
  const [userService, setUserService] = useState(null);

  const fetchServices = async () => {
    await getServices();
    const selected = getSelectedServices();

    setUserService(selected);
  };

  useEffect(() => {
    fetchServices();
  }, []);

  return userService;
}
