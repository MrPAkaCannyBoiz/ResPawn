import { defineConfig } from 'vitest/config'

export default defineConfig({
  test: {
    include: ['__tests__/integration/**/*.test.js'],
    testTimeout: 30000,
  },
})
