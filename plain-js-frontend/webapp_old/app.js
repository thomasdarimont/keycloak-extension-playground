var keycloak = new Keycloak({
  url: 'http://localhost:8081/auth',
  realm: 'requirerole',
  clientId: 'app-frontend-plainjs'
});

window.onload = function () {

  keycloak.init({onLoad: 'login-required', checkLoginIframe: true, checkLoginIframeInterval: 1})
    .success(function () {

      if (keycloak.authenticated) {
        showProfile();
      } else {
        welcome();
      }

      document.body.style.display = 'block';
    });
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

keycloak.onAuthLogout = welcome;
