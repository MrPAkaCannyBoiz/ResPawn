import { createContext, useReducer, useMemo } from 'react'

export const CartContext = createContext(null)

function cartReducer(state, action) {
  switch (action.type) {
    case 'ADD_ITEM': {
      const existing = state.find((i) => i.productId === action.payload.productId)
      if (existing) {
        return state.map((i) =>
          i.productId === action.payload.productId
            ? { ...i, quantity: i.quantity + 1 }
            : i
        )
      }
      return [...state, { ...action.payload, quantity: 1 }]
    }
    case 'REMOVE_ITEM':
      return state.filter((i) => i.productId !== action.payload)
    case 'SET_QUANTITY':
      return state.map((i) =>
        i.productId === action.payload.productId
          ? { ...i, quantity: Math.max(1, action.payload.quantity) }
          : i
      )
    case 'CLEAR':
      return []
    default:
      return state
  }
}

export function CartProvider({ children }) {
  const [items, dispatch] = useReducer(cartReducer, [])

  const addItem = (productId, name, price, imageUrl) => {
    dispatch({ type: 'ADD_ITEM', payload: { productId, name, price, imageUrl } })
  }

  const removeItem = (productId) => {
    dispatch({ type: 'REMOVE_ITEM', payload: productId })
  }

  const setQuantity = (productId, quantity) => {
    dispatch({ type: 'SET_QUANTITY', payload: { productId, quantity } })
  }

  const clear = () => {
    dispatch({ type: 'CLEAR' })
  }

  const totalQuantity = useMemo(() => items.reduce((sum, i) => sum + i.quantity, 0), [items])
  const totalPrice = useMemo(() => items.reduce((sum, i) => sum + i.price * i.quantity, 0), [items])

  const value = { items, addItem, removeItem, setQuantity, clear, totalQuantity, totalPrice }

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}
