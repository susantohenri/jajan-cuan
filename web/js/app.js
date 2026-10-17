// Jajan Cuan - Landing Page Logic (Modal & Waitlist Submission)
document.addEventListener('DOMContentLoaded', () => {
  const modal = document.getElementById('waitlistModal');
  const closeModalBtn = document.getElementById('closeModalBtn');
  const waitlistForm = document.getElementById('waitlistForm');
  const contactInput = document.getElementById('contactInput');
  const formContainer = document.getElementById('modalFormContainer');
  const successContainer = document.getElementById('modalSuccessContainer');
  const submitBtn = document.getElementById('submitWaitlistBtn');
  const radioButtons = document.querySelectorAll('input[name="contactType"]');
  const triggers = document.querySelectorAll('.trigger-waitlist');

  let currentSource = 'hero_badge';

  // Open modal with dynamic source
  function openModal(source = 'general') {
    currentSource = source;
    if (modal) {
      modal.classList.add('active');
      modal.setAttribute('aria-hidden', 'false');
      // Reset view to form
      if (formContainer && successContainer) {
        formContainer.style.display = 'block';
        successContainer.classList.remove('active');
      }
      if (contactInput) {
        setTimeout(() => contactInput.focus(), 100);
      }
    }
  }

  // Close modal
  function closeModal() {
    if (modal) {
      modal.classList.remove('active');
      modal.setAttribute('aria-hidden', 'true');
      if (waitlistForm) {
        waitlistForm.reset();
      }
      if (submitBtn) {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Kabari Saya Saat Rilis →';
      }
    }
  }

  // Attach trigger click listeners
  triggers.forEach((btn) => {
    btn.addEventListener('click', (e) => {
      e.preventDefault();
      const source = btn.getAttribute('data-source') || 'general';
      openModal(source);
    });
  });

  // Close on close button click
  if (closeModalBtn) {
    closeModalBtn.addEventListener('click', closeModal);
  }

  // Close on backdrop click (click outside card)
  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) {
        closeModal();
      }
    });
  }

  // Close on Escape key
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape' && modal && modal.classList.contains('active')) {
      closeModal();
    }
  });

  // Dynamic input placeholder based on radio selection
  radioButtons.forEach((radio) => {
    radio.addEventListener('change', (e) => {
      if (e.target.value === 'email') {
        contactInput.placeholder = 'Alamat Email (contoh: nama@gmail.com)';
        contactInput.type = 'email';
      } else {
        contactInput.placeholder = 'Nomor WhatsApp (contoh: 0812xxxx)';
        contactInput.type = 'text';
      }
    });
  });

  // Form submit handler
  if (waitlistForm) {
    waitlistForm.addEventListener('submit', async (e) => {
      e.preventDefault();
      const contactVal = contactInput.value.trim();
      const selectedType = document.querySelector('input[name="contactType"]:checked')?.value || 'whatsapp';

      // Validation: minimal 5 karakter
      if (contactVal.length < 5) {
        alert('Mohon masukkan nomor WhatsApp atau email yang valid.');
        contactInput.focus();
        return;
      }

      // UI loading state
      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.textContent = 'Menyimpan...';
      }

      const payload = {
        contact: contactVal,
        type: selectedType,
        source: currentSource,
        created_at: new Date().toISOString()
      };

      try {
        // Push to Firebase RTDB node /waitlist
        if (typeof db !== 'undefined') {
          await db.ref('waitlist').push(payload);
        } else {
          console.warn('Firebase db instance not found, running simulated submission.');
        }

        // Fire GA4 Event
        if (typeof gtag === 'function') {
          gtag('event', 'lead_captured', {
            contact_type: selectedType,
            source: currentSource
          });
        }

        // Show Success state
        if (formContainer && successContainer) {
          formContainer.style.display = 'none';
          successContainer.classList.add('active');
        }

        // Auto close after 2.5s
        setTimeout(() => {
          closeModal();
        }, 2500);

      } catch (error) {
        console.error('Error submitting waitlist:', error);
        alert('Terjadi kesalahan saat menyimpan data. Silakan coba lagi.');
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.textContent = 'Kabari Saya Saat Rilis →';
        }
      }
    });
  }
});
