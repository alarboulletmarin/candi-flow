import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

// Implémentation de localStorage comme fallback pour le web
class WebStorage {
  async getItemAsync(key: string): Promise<string | null> {
    return localStorage.getItem(key);
  }

  async setItemAsync(key: string, value: string): Promise<void> {
    localStorage.setItem(key, value);
  }

  async deleteItemAsync(key: string): Promise<void> {
    localStorage.removeItem(key);
  }
}

// Utiliser SecureStore pour mobile ou notre implémentation WebStorage pour web
const storage = Platform.OS === 'web' ? new WebStorage() : SecureStore;

export default storage;
