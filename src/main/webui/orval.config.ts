import { defineConfig } from 'orval';

export default defineConfig({
  apiClient: {
    input: './openapi.yaml',
    output: {
      target: './src/apiClient.ts',
      schemas: './src/model',
      httpClient: 'fetch',
      client: 'vue-query',
      prettier: true,
      mock: false,
      mode: 'split',
    },
  },
});