import { Outlet } from 'react-router-dom'
import Navbar from './Navbar'

export default function MainLayout() {
  return (
    <div className="layout-root">
      <Navbar />
      <main className="content-area">
        <Outlet />
      </main>
    </div>
  )
}
