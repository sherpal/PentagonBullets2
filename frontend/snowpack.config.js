const httpProxy = require("http-proxy")
const proxy = httpProxy.createServer({ target: "http://localhost:9000" })

module.exports = {
  packageOptions: {
    polyfillNode: true,
  },
  buildOptions: {
    out: "../server/src/main/resources/public",
  },
  mount: {
    public: "/",
    "target/scala-3.1.0/frontend-fastopt": "/",
    "src/main/resources": "/",
  },

  routes: [
    {
      src: "/api/.*",
      dest: (req, res) => {
        // remove /api prefix (optional)
        //req.url = req.url.replace(/^\/api/, '');
        proxy.web(req, res)
      },
    },
    {
      src: "/ws/.*",
      upgrade: (req, res) => {
        proxy.ws(req, res)
      },
    },
    { match: "routes", src: ".*", dest: "/index.html" },
    // {
    //   src: ".*/.*(js|map|png|css|json)$",
    //   dest: (req, res) => {
    //     proxy.web(req, res);
    //   },
    // },
    // {
    //   src: ".*",
    //   dest: (req, res) => {
    //     console.log("url was", req.url);
    //     req.url = "/index.html";
    //     proxy.web(req, res);
    //   },
    // },
  ],
}
