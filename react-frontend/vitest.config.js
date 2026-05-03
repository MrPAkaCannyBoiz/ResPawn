import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/setupTests.js'],
    exclude: ['__tests__/integration/**', 'node_modules/**'],
  },
})
