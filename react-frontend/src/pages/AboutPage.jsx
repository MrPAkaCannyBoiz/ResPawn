import '../styles/about.css'

export default function AboutPage() {
  return (
    <div className="about-shell">
      <div className="about-card">
        <h1 className="about-title">About ResPawn</h1>

        <section className="about-section">
          <h2>What is ResPawn?</h2>
          <p>
            ResPawn is an online pawn shop platform for buying and selling second-hand items.
            Customers can list products, browse available items, and purchase securely.
            Resellers inspect and verify product quality before items go live.
          </p>
        </section>

        <section className="about-section">
          <h2>Academic Origin</h2>
          <p>
            This project started as a 3rd-semester university assignment (SEP3) at VIA University College,
            demonstrating heterogeneous system integration across C# and Java with gRPC communication,
            a Blazor frontend, and a PostgreSQL database.
          </p>
          <a
            href="https://github.com/MrPAkaCannyBoiz/ResPawnMarket"
            target="_blank"
            rel="noopener noreferrer"
            className="about-link"
          >
            View the original academic repository &rarr;
          </a>
        </section>

        <section className="about-section">
          <h2>Evolution to Production</h2>
          <p>
            Beyond the academic scope, I rebuilt and extended the project with real-world
            features and platform engineering practices:
          </p>
          <ul className="about-feature-list">
            <li>React frontend replacing the original Blazor client</li>
            <li>RabbitMQ async messaging for welcome emails</li>
            <li>CI/CD pipeline with test coverage gates</li>
            <li>Docker containerization for all services</li>
            <li>VPS deployment with Caddy reverse proxy and automatic TLS</li>
            <li>Cloudflare Pages for frontend hosting</li>
          </ul>
        </section>

        <section className="about-section">
          <h2>Tech Stack</h2>
          <div className="tech-stack-grid">
            <span className="tech-badge">React</span>
            <span className="tech-badge">.NET</span>
            <span className="tech-badge">Spring Boot</span>
            <span className="tech-badge">gRPC</span>
            <span className="tech-badge">PostgreSQL</span>
            <span className="tech-badge">RabbitMQ</span>
            <span className="tech-badge">Docker</span>
            <span className="tech-badge">Caddy</span>
          </div>
        </section>
      </div>
    </div>
  )
}
