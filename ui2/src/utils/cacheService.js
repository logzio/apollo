import lscache from 'lscache';

export const setToCache = (key, valueToStore, minutesToLive) => lscache.set(key, valueToStore, minutesToLive);
export const getFromCache = key => lscache.get(key);
export const removeFromCache = key => lscache.remove(key);

export const fetchAndStore = async (key, getterFunc, minutesToLive, params) => {
  const storedValue = getFromCache(key);
  if (storedValue) {
    return storedValue;
  }
  const valueToStore = await getterFunc(params);
  setToCache(key, valueToStore, minutesToLive);
  return valueToStore;
};

export const clearExpiredCache = () => lscache.flushExpired();

export const clearCache = () => lscache.flush();
