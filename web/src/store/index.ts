import { createPinia } from 'pinia'
import { usePersist } from 'pinia-use-persist'
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'

const store = createPinia()
store.use(piniaPluginPersistedstate)
// store.use(usePersist)

export default store
