console.log("init identity first");

const KC_LOGIN_FORM_ID = "kc-form-login";
const KC_CONTENT_WRAPPER = "kc-content-wrapper";

function makeAsync(form) {
    form.addEventListener('submit', e => {

        // Store reference to form to make later code easier to read
        const form = e.target;

        // Prevent the default form submit
        e.preventDefault();

        let params = new URLSearchParams(new FormData(form));
        if (form.intent) {
            params.append(form.intent, "");
        }

        // Post data using the Fetch API
        fetch(form.action, {
            method: form.method,
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: params
        })
        // If we got a redirect, we follow it
            .then(res => res.redirected ? window.location = res.url : res)
            // We turn the response into text as we expect HTML
            .then(res => res.text())

            // Let's turn it into an HTML document
            .then(text => new DOMParser().parseFromString(text, 'text/html'))

            // Now we have a document to work with let's replace the <form>
            .then(doc => {

                let oldWrapper = document.getElementById(KC_CONTENT_WRAPPER);
                let newWrapper = doc.getElementById(KC_CONTENT_WRAPPER);

                oldWrapper.parentNode.replaceChild(newWrapper, oldWrapper);

                makeAsync(document.getElementById(KC_LOGIN_FORM_ID));
            });
    });
}

makeAsync(document.getElementById(KC_LOGIN_FORM_ID));