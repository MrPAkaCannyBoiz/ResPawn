import { lazy, Suspense } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import MainLayout from './components/layout/MainLayout'
import ProtectedRoute from './components/layout/ProtectedRoute'
import LoadingSpinner from './components/shared/LoadingSpinner'

const HomePage = lazy(() => import('./pages/HomePage'))
const AvailableProductsPage = lazy(() => import('./pages/AvailableProductsPage'))
const ProductDetailsPage = lazy(() => import('./pages/ProductDetailsPage'))
const CustomerLoginPage = lazy(() => import('./pages/CustomerLoginPage'))
const ResellerLoginPage = lazy(() => import('./pages/ResellerLoginPage'))
const RegisterPage = lazy(() => import('./pages/RegisterPage'))
const PurchasePage = lazy(() => import('./pages/PurchasePage'))
const UploadProductPage = lazy(() => import('./pages/UploadProductPage'))
const MyProductsPage = lazy(() => import('./pages/MyProductsPage'))
const CustomerProfilePage = lazy(() => import('./pages/CustomerProfilePage'))
const UpdateProfilePage = lazy(() => import('./pages/UpdateProfilePage'))
const ResellerCheckerPage = lazy(() => import('./pages/ResellerCheckerPage'))
const ProductReviewPage = lazy(() => import('./pages/ProductReviewPage'))
const CustomerInspectionPage = lazy(() => import('./pages/CustomerInspectionPage'))

function SuspenseWrap({ children }) {
  return <Suspense fallback={<LoadingSpinner />}>{children}</Suspense>
}

const router = createBrowserRouter([
  {
    element: <MainLayout />,
    children: [
      { path: '/', element: <SuspenseWrap><HomePage /></SuspenseWrap> },
      { path: '/available-product', element: <SuspenseWrap><AvailableProductsPage /></SuspenseWrap> },
      { path: '/products/:id', element: <SuspenseWrap><ProductDetailsPage /></SuspenseWrap> },
      { path: '/customer-login', element: <SuspenseWrap><CustomerLoginPage /></SuspenseWrap> },
      { path: '/reseller-login', element: <SuspenseWrap><ResellerLoginPage /></SuspenseWrap> },
      { path: '/register', element: <SuspenseWrap><RegisterPage /></SuspenseWrap> },
      {
        path: '/purchase',
        element: (
          <ProtectedRoute allowedRoles={['Customer']}>
            <SuspenseWrap><PurchasePage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/customer/new-product',
        element: (
          <ProtectedRoute allowedRoles={['Customer']} requireCanSell>
            <SuspenseWrap><UploadProductPage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/my-products',
        element: (
          <ProtectedRoute allowedRoles={['Customer']}>
            <SuspenseWrap><MyProductsPage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/customer/info',
        element: (
          <ProtectedRoute allowedRoles={['Customer']}>
            <SuspenseWrap><CustomerProfilePage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/customer/info/update',
        element: (
          <ProtectedRoute allowedRoles={['Customer']}>
            <SuspenseWrap><UpdateProfilePage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/resellercheck',
        element: (
          <ProtectedRoute allowedRoles={['Reseller']}>
            <SuspenseWrap><ResellerCheckerPage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/reseller/product/:id',
        element: (
          <ProtectedRoute allowedRoles={['Reseller']}>
            <SuspenseWrap><ProductReviewPage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
      {
        path: '/customer/:id/inspection',
        element: (
          <ProtectedRoute allowedRoles={['Reseller']}>
            <SuspenseWrap><CustomerInspectionPage /></SuspenseWrap>
          </ProtectedRoute>
        ),
      },
    ],
  },
])

export default function App() {
  return <RouterProvider router={router} />
}
