let keycloakUrl = "http://localhost:8081/auth"

var script = document.createElement('script');
script.type = 'text/javascript';
script.src = keycloakUrl+"/js/keycloak.js";

document.getElementsByTagName('head')[0].appendChild(script);

window.onload = function () {

  window.keycloak = new Keycloak({
    url: keycloakUrl,
    realm: 'access-policy-authenticator-demo',
    clientId: 'app-plainjs-spa-fe'
  });


  keycloak.init({onLoad: 'login-required', checkLoginIframe: true, checkLoginIframeInterval: 1, pkceMethod: 'S256'})
    .success(function () {

      if (keycloak.authenticated) {
        showProfile();
      } else {
        welcome();
      }

      document.body.style.display = 'block';
    });

  keycloak.onAuthLogout = welcome;
};

function welcome() {
  show('welcome');
}

function showProfile() {

  if (keycloak.tokenParsed['given_name']) {
    document.getElementById('firstName').innerHTML = keycloak.tokenParsed['given_name'];
  }
  if (keycloak.tokenParsed['family_name']) {
    document.getElementById('lastName').innerHTML = keycloak.tokenParsed['family_name'];
  }
  if (keycloak.tokenParsed['preferred_username']) {
    document.getElementById('username').innerHTML = keycloak.tokenParsed['preferred_username'];
  }
  if (keycloak.tokenParsed['email']) {
    document.getElementById('email').innerHTML = keycloak.tokenParsed['email'];
  }

  show('profile');
}

function showToken() {
  document.getElementById('token-content').innerHTML = JSON.stringify(keycloak.tokenParsed, null, '    ');
  show('token');
}

function showIdToken() {
  document.getElementById('token-content').innerHTML = JSON.stringify(keycloak.idTokenParsed, null, '    ');
  show('token');
}

function show(id) {
  document.getElementById('welcome').style.display = 'none';
  document.getElementById('profile').style.display = 'none';
  document.getElementById('token').style.display = 'none';
  document.getElementById('idToken').style.display = 'none';
  document.getElementById(id).style.display = 'block';
}

