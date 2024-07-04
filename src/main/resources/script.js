function createForm() {
	    console.log('Creating form...');  // Debug message
    const formContainer = document.getElementById('dynamicForm');

    // Create form element
    const form = document.createElement('form');
    form.id = 'nanoForm';
    form.method = 'post';

    // Create input element for Nano address
    const nanoAddressLabel = document.createElement('label');
    nanoAddressLabel.textContent = 'Nano Address:';
    const nanoAddressInput = document.createElement('input');
    nanoAddressInput.type = 'text';
    nanoAddressInput.id = 'destination';
    nanoAddressInput.name = 'destination';
    nanoAddressInput.required = true;

    // Append input and label to the form
    form.appendChild(nanoAddressLabel);
    form.appendChild(nanoAddressInput);

    // Append the form to the container
    formContainer.appendChild(form);
}

function submitForm() {
    const destination = document.getElementById('destination').value;

    grecaptcha.ready(function () {
        // Request a reCAPTCHA token
        grecaptcha.execute('6Le3sm4pAAAAAJIkQWdAX4cuPjDrfdk_1-lErb8X', { action: 'submit_form' }).then(
            function (token) {
                // Include the token in your form submission
                const formData = new FormData();
                formData.append('destination', destination);
                formData.append('g-recaptcha-response', token);

                // Send the form data to the server using fetch
                fetch('/send', {
                    method: 'POST',
                    body: formData,
                })
                .then(response => response.json())
                .then(data => {
                    // Update the content dynamically with the response
                    const responseContainer = document.getElementById('responseContainer');
                    responseContainer.innerHTML = JSON.stringify(data, null, 2);
                })
                .catch(error => {
                    console.error('Error:', error);
                    // Handle error if necessary
                });
            },
            function (error) {
                // Handle reCAPTCHA execution error
                console.error('reCAPTCHA execution error:', error);
                // You may want to inform the user or take alternative actions
            }
        );
    });
}

window.onload = function () {
	console.log('Page loaded.');  // Debug message
    createForm();
};


