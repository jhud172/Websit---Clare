const siteHeader = document.querySelector("[data-site-header]");
const navToggle = document.querySelector("[data-nav-toggle]");
const siteNav = document.querySelector("[data-site-nav]");
const enquiryModal = document.querySelector("[data-enquiry-modal]");
const themeToggle = document.querySelector("[data-theme-toggle]");
const themeToggleLabel = document.querySelector("[data-theme-toggle-label]");
const fileInputs = document.querySelectorAll("[data-file-input]");
const THEME_STORAGE_KEY = "clare-theme";
const revealSelectors = [
    ".page-hero-grid > *",
    ".hero-copy",
    ".hero .visual-stage",
    ".section-heading",
    ".path-panel",
    ".detail-card",
    ".quote-card",
    ".package-card",
    ".gallery-card",
    ".story-copy",
    ".split-layout .visual-stage",
    ".process-list li",
    ".faq-list article",
    ".contact-panel",
    ".contact-form",
    ".cta-panel",
    ".policy-index",
    ".policy-card"
];

const modalTriggerSelector = "[data-open-enquiry-modal]";

const applyTheme = (theme) => {
    document.documentElement.dataset.theme = theme;

    if (!themeToggle || !themeToggleLabel) {
        return;
    }

    const nextTheme = theme === "dark" ? "light" : "dark";
    const nextLabel = nextTheme === "dark" ? "Dark mode" : "Light mode";

    themeToggle.setAttribute("aria-pressed", String(theme === "dark"));
    themeToggle.setAttribute("aria-label", `Switch to ${nextLabel.toLowerCase()}`);
    themeToggleLabel.textContent = nextLabel;
};

const resolveTheme = () => {
    if (!themeToggle) {
        return "light";
    }

    const storedTheme = window.localStorage.getItem(THEME_STORAGE_KEY);
    if (storedTheme === "light" || storedTheme === "dark") {
        return storedTheme;
    }

    return window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
};

applyTheme(resolveTheme());

if (themeToggle) {
    themeToggle.addEventListener("click", () => {
        const nextTheme = document.documentElement.dataset.theme === "dark" ? "light" : "dark";
        window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
        applyTheme(nextTheme);
    });

    window.matchMedia("(prefers-color-scheme: dark)").addEventListener("change", (event) => {
        if (window.localStorage.getItem(THEME_STORAGE_KEY)) {
            return;
        }

        applyTheme(event.matches ? "dark" : "light");
    });
}

if (siteHeader) {
    let lastScrollY = window.scrollY;
    let lastDirection = "up";

    const syncHeader = () => {
        const currentScrollY = window.scrollY;
        const scrollDelta = currentScrollY - lastScrollY;
        const scrollingDown = scrollDelta > 4;
        const scrollingUp = scrollDelta < -4;

        siteHeader.classList.toggle("is-scrolled", currentScrollY > 16);

        if (scrollingDown) {
            lastDirection = "down";
        }
        else if (scrollingUp) {
            lastDirection = "up";
        }

        siteHeader.classList.toggle("is-scroll-up", currentScrollY > 96 && lastDirection === "up");
        siteHeader.classList.toggle("is-scroll-down", currentScrollY > 180 && lastDirection === "down");

        if (window.innerWidth > 1040) {
            if (currentScrollY > 180 && lastDirection === "down") {
                siteHeader.classList.add("is-condensed");
            }
            else if (currentScrollY < 96 || lastDirection === "up") {
                siteHeader.classList.remove("is-condensed");
            }
        }
        else {
            siteHeader.classList.remove("is-condensed");
        }

        lastScrollY = currentScrollY;
    };

    syncHeader();
    window.addEventListener("scroll", syncHeader, { passive: true });
}

if (navToggle && siteNav) {
    const closeNav = () => {
        siteNav.classList.remove("is-open");
        navToggle.setAttribute("aria-expanded", "false");
    };

    navToggle.addEventListener("click", () => {
        const isOpen = siteNav.classList.toggle("is-open");
        navToggle.setAttribute("aria-expanded", String(isOpen));
    });

    siteNav.querySelectorAll("a").forEach((link) => {
        link.addEventListener("click", closeNav);
    });

    window.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeNav();
        }
    });

    window.addEventListener("resize", () => {
        if (window.innerWidth > 1040) {
            closeNav();
        }
    });

    document.addEventListener("click", (event) => {
        if (siteHeader && !siteHeader.contains(event.target) && siteNav.classList.contains("is-open")) {
            closeNav();
        }
    });
}

if (enquiryModal) {
    const modalDialog = enquiryModal.querySelector("[data-enquiry-modal-dialog]");
    const modalCloseButtons = enquiryModal.querySelectorAll("[data-enquiry-modal-close]");
    const modalTriggers = document.querySelectorAll(modalTriggerSelector);
    let previousFocus = null;

    const getFocusableElements = () => Array.from(modalDialog.querySelectorAll(
        "a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex='-1'])"
    )).filter((element) => !element.hasAttribute("hidden"));

    const lockScroll = (locked) => {
        document.body.classList.toggle("is-modal-open", locked);
    };

    const closeModal = () => {
        enquiryModal.hidden = true;
        enquiryModal.classList.remove("is-open");
        lockScroll(false);
        if (previousFocus && typeof previousFocus.focus === "function") {
            previousFocus.focus({ preventScroll: true });
        }
    };

    const openModal = (trigger) => {
        previousFocus = trigger || document.activeElement;
        enquiryModal.hidden = false;
        enquiryModal.classList.add("is-open");
        lockScroll(true);

        const serviceType = trigger?.dataset.enquiryService;
        const serviceField = modalDialog.querySelector("#serviceType");
        if (serviceType && serviceField) {
            serviceField.value = serviceType;
            serviceField.dispatchEvent(new Event("change", { bubbles: true }));
        }

        window.setTimeout(() => {
            const focusTargets = getFocusableElements();
            if (focusTargets.length > 0) {
                focusTargets[0].focus({ preventScroll: true });
            }
            else {
                modalDialog.focus({ preventScroll: true });
            }
        }, 20);
    };

    modalTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => openModal(trigger));
    });

    modalCloseButtons.forEach((button) => {
        button.addEventListener("click", closeModal);
    });

    enquiryModal.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            event.preventDefault();
            closeModal();
            return;
        }

        if (event.key !== "Tab") {
            return;
        }

        const focusables = getFocusableElements();
        if (focusables.length === 0) {
            event.preventDefault();
            return;
        }

        const first = focusables[0];
        const last = focusables[focusables.length - 1];

        if (event.shiftKey && document.activeElement === first) {
            event.preventDefault();
            last.focus();
        }
        else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    });
}

document.querySelectorAll("[data-carousel]").forEach((carouselRoot) => {
    const slides = Array.from(carouselRoot.querySelectorAll("[data-carousel-slide]"));
    const dots = Array.from(carouselRoot.querySelectorAll("[data-carousel-dot]"));
    const prevButton = carouselRoot.querySelector("[data-carousel-prev]");
    const nextButton = carouselRoot.querySelector("[data-carousel-next]");
    const intervalMs = Number(carouselRoot.dataset.carouselInterval || 6000);
    let currentIndex = 0;
    let timerId = null;

    if (slides.length === 0) {
        return;
    }

    const render = () => {
        slides.forEach((slide, index) => {
            const active = index === currentIndex;
            slide.classList.toggle("is-active", active);
            slide.setAttribute("aria-hidden", String(!active));
        });

        dots.forEach((dot, index) => {
            const active = index === currentIndex;
            dot.classList.toggle("is-active", active);
            dot.setAttribute("aria-current", active ? "true" : "false");
        });
    };

    const moveTo = (nextIndex) => {
        currentIndex = (nextIndex + slides.length) % slides.length;
        render();
    };

    const restartTimer = () => {
        if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
            return;
        }

        if (timerId) {
            window.clearInterval(timerId);
        }

        timerId = window.setInterval(() => {
            moveTo(currentIndex + 1);
        }, intervalMs);
    };

    prevButton?.addEventListener("click", () => {
        moveTo(currentIndex - 1);
        restartTimer();
    });

    nextButton?.addEventListener("click", () => {
        moveTo(currentIndex + 1);
        restartTimer();
    });

    dots.forEach((dot, index) => {
        dot.addEventListener("click", () => {
            moveTo(index);
            restartTimer();
        });
    });

    carouselRoot.addEventListener("mouseenter", () => {
        if (timerId) {
            window.clearInterval(timerId);
        }
    });

    carouselRoot.addEventListener("mouseleave", restartTimer);

    render();
    restartTimer();
});

document.querySelectorAll(".protected-image, .hero-media img, .showcase-slide img").forEach((image) => {
    image.setAttribute("draggable", "false");

    image.addEventListener("dragstart", (event) => {
        event.preventDefault();
    });

    image.addEventListener("contextmenu", (event) => {
        event.preventDefault();
    });
});

const revealTargets = Array.from(new Set(
    revealSelectors.flatMap((selector) => Array.from(document.querySelectorAll(selector)))
));

if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
    revealTargets.forEach((element) => {
        element.classList.add("is-visible");
    });
}
else if (revealTargets.length > 0) {
    revealTargets.forEach((element, index) => {
        element.classList.add("reveal-ready");
        element.classList.add(`reveal-delay-${index % 4}`);
    });

    const revealObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach((entry) => {
            if (!entry.isIntersecting) {
                return;
            }

            entry.target.classList.add("is-visible");
            observer.unobserve(entry.target);
        });
    }, {
        threshold: 0.18,
        rootMargin: "0px 0px -8% 0px"
    });

    revealTargets.forEach((element) => {
        revealObserver.observe(element);
    });
}

const formatFileSize = (size) => {
    if (size < 1024 * 1024) {
        return `${Math.round(size / 1024)} KB`;
    }

    return `${(size / (1024 * 1024)).toFixed(1)} MB`;
};

fileInputs.forEach((input) => {
    const targetId = input.getAttribute("data-file-list-target");
    const fileList = targetId ? document.getElementById(targetId) : null;
    const dropzone = input.closest("[data-file-dropzone]");

    const syncFiles = () => {
        if (!dropzone || !fileList) {
            return;
        }

        const files = Array.from(input.files || []);
        dropzone.classList.toggle("has-files", files.length > 0);
        fileList.innerHTML = "";
        fileList.hidden = files.length === 0;

        files.forEach((file) => {
            const item = document.createElement("li");
            item.textContent = `${file.name} · ${formatFileSize(file.size)}`;
            fileList.appendChild(item);
        });
    };

    syncFiles();
    input.addEventListener("change", syncFiles);
});

const formatPaddedNumber = (value) => String(value).padStart(2, "0");

const parseIsoDate = (value) => {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) {
        return null;
    }

    const [year, month, day] = value.split("-").map(Number);
    const date = new Date(year, month - 1, day);

    if (
        Number.isNaN(date.getTime())
        || date.getFullYear() !== year
        || date.getMonth() !== month - 1
        || date.getDate() !== day
    ) {
        return null;
    }

    return date;
};

const toIsoDate = (date) => `${date.getFullYear()}-${formatPaddedNumber(date.getMonth() + 1)}-${formatPaddedNumber(date.getDate())}`;

const formatDisplayDate = (date) => `${formatPaddedNumber(date.getDate())}/${formatPaddedNumber(date.getMonth() + 1)}/${date.getFullYear()}`;

const isSameDay = (left, right) => left && right
    && left.getFullYear() === right.getFullYear()
    && left.getMonth() === right.getMonth()
    && left.getDate() === right.getDate();

const formatMonthLabel = (date) => new Intl.DateTimeFormat("en-GB", {
    month: "long",
    year: "numeric"
}).format(date);

const customFieldRoots = Array.from(document.querySelectorAll("[data-choice-select], [data-date-picker], [data-phone-field]"));

const positionCustomField = (root) => {
    const popup = root.querySelector(".field-popup");

    if (!popup) {
        return;
    }

    popup.classList.remove("is-above");
    popup.style.removeProperty("--field-popup-max-height");

    const rootRect = root.getBoundingClientRect();
    const popupRect = popup.getBoundingClientRect();
    const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
    const spaceBelow = viewportHeight - rootRect.bottom - 18;
    const spaceAbove = rootRect.top - 18;
    const openAbove = spaceBelow < Math.min(popupRect.height, 300) && spaceAbove > spaceBelow;
    const availableSpace = Math.max(180, Math.floor(openAbove ? spaceAbove : spaceBelow));

    popup.classList.toggle("is-above", openAbove);
    popup.style.setProperty("--field-popup-max-height", `${availableSpace}px`);
};

const closeCustomField = (root) => {
    const trigger = root.querySelector("[data-choice-trigger], [data-date-trigger], [data-phone-code-trigger]");

    root.classList.remove("is-open");
    root.querySelector(".field-popup")?.classList.remove("is-above");

    if (trigger) {
        trigger.setAttribute("aria-expanded", "false");
    }
};

const openCustomField = (root) => {
    customFieldRoots.forEach((candidate) => {
        if (candidate !== root) {
            closeCustomField(candidate);
        }
    });

    const trigger = root.querySelector("[data-choice-trigger], [data-date-trigger], [data-phone-code-trigger]");

    root.classList.add("is-open");

    if (trigger) {
        trigger.setAttribute("aria-expanded", "true");
    }

    window.requestAnimationFrame(() => positionCustomField(root));
};

document.addEventListener("click", (event) => {
    customFieldRoots.forEach((root) => {
        if (!root.contains(event.target)) {
            closeCustomField(root);
        }
    });
});

document.addEventListener("keydown", (event) => {
    if (event.key !== "Escape") {
        return;
    }

    customFieldRoots.forEach((root) => {
        if (root.classList.contains("is-open")) {
            closeCustomField(root);
        }
    });
});

window.addEventListener("resize", () => {
    customFieldRoots.forEach((root) => {
        if (root.classList.contains("is-open")) {
            positionCustomField(root);
        }
    });
});

window.addEventListener("scroll", () => {
    customFieldRoots.forEach((root) => {
        if (root.classList.contains("is-open")) {
            positionCustomField(root);
        }
    });
}, { passive: true });

document.querySelectorAll("[data-choice-select]").forEach((root) => {
    const input = root.querySelector("[data-choice-input]");
    const trigger = root.querySelector("[data-choice-trigger]");
    const label = root.querySelector("[data-choice-label]");
    const options = Array.from(root.querySelectorAll("[data-choice-option]"));
    const placeholder = trigger?.dataset.choicePlaceholder || "";

    if (!input || !trigger || !label || options.length === 0) {
        return;
    }

    const syncChoice = () => {
        const selected = options.find((option) => option.dataset.value === input.value);
        const labelText = selected ? selected.textContent.trim() : placeholder;

        label.textContent = labelText;
        trigger.classList.toggle("is-placeholder", !selected || !input.value);

        options.forEach((option) => {
            const isSelected = option.dataset.value === input.value;
            option.classList.toggle("is-selected", isSelected);
            option.setAttribute("aria-selected", String(isSelected));
        });
    };

    trigger.addEventListener("click", () => {
        if (root.classList.contains("is-open")) {
            closeCustomField(root);
            return;
        }

        openCustomField(root);
    });

    options.forEach((option) => {
        option.addEventListener("click", () => {
            input.value = option.dataset.value || "";
            input.dispatchEvent(new Event("change", { bubbles: true }));
            syncChoice();
            closeCustomField(root);
            trigger.focus();
        });
    });

    syncChoice();
});

document.querySelectorAll("[data-date-picker]").forEach((root) => {
    const input = root.querySelector("[data-date-input]");
    const trigger = root.querySelector("[data-date-trigger]");
    const label = root.querySelector("[data-date-label]");
    const monthLabel = root.querySelector("[data-date-month]");
    const grid = root.querySelector("[data-date-grid]");
    const prevButton = root.querySelector('[data-date-nav="prev"]');
    const nextButton = root.querySelector('[data-date-nav="next"]');
    const clearButton = root.querySelector('[data-date-action="clear"]');
    const todayButton = root.querySelector('[data-date-action="today"]');
    const placeholder = trigger?.dataset.datePlaceholder || "dd/mm/yyyy";

    if (!input || !trigger || !label || !monthLabel || !grid || !prevButton || !nextButton || !clearButton || !todayButton) {
        return;
    }

    let selectedDate = parseIsoDate(input.value);
    let viewDate = selectedDate ? new Date(selectedDate.getFullYear(), selectedDate.getMonth(), 1) : new Date(new Date().getFullYear(), new Date().getMonth(), 1);

    const syncDateLabel = () => {
        selectedDate = parseIsoDate(input.value);
        label.textContent = selectedDate ? formatDisplayDate(selectedDate) : placeholder;
        trigger.classList.toggle("is-placeholder", !selectedDate);
    };

    const renderCalendar = () => {
        monthLabel.textContent = formatMonthLabel(viewDate);
        grid.innerHTML = "";

        const today = new Date();
        const monthStart = new Date(viewDate.getFullYear(), viewDate.getMonth(), 1);
        const weekdayOffset = (monthStart.getDay() + 6) % 7;
        const firstVisibleDate = new Date(viewDate.getFullYear(), viewDate.getMonth(), 1 - weekdayOffset);

        for (let index = 0; index < 42; index += 1) {
            const date = new Date(firstVisibleDate.getFullYear(), firstVisibleDate.getMonth(), firstVisibleDate.getDate() + index);
            const dayButton = document.createElement("button");

            dayButton.type = "button";
            dayButton.className = "calendar-day";
            dayButton.textContent = String(date.getDate());
            dayButton.dataset.dateValue = toIsoDate(date);
            dayButton.setAttribute("role", "gridcell");

            if (date.getMonth() !== viewDate.getMonth()) {
                dayButton.classList.add("is-outside-month");
            }

            if (isSameDay(date, today)) {
                dayButton.classList.add("is-today");
            }

            if (selectedDate && isSameDay(date, selectedDate)) {
                dayButton.classList.add("is-selected");
            }

            dayButton.addEventListener("click", () => {
                input.value = dayButton.dataset.dateValue;
                input.dispatchEvent(new Event("change", { bubbles: true }));
                syncDateLabel();
                viewDate = new Date(date.getFullYear(), date.getMonth(), 1);
                renderCalendar();
                closeCustomField(root);
                trigger.focus();
            });

            grid.appendChild(dayButton);
        }
    };

    trigger.addEventListener("click", () => {
        if (root.classList.contains("is-open")) {
            closeCustomField(root);
            return;
        }

        const currentValue = parseIsoDate(input.value);
        const today = new Date();
        viewDate = currentValue
            ? new Date(currentValue.getFullYear(), currentValue.getMonth(), 1)
            : new Date(today.getFullYear(), today.getMonth(), 1);
        renderCalendar();
        openCustomField(root);
    });

    prevButton.addEventListener("click", () => {
        viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() - 1, 1);
        renderCalendar();
    });

    nextButton.addEventListener("click", () => {
        viewDate = new Date(viewDate.getFullYear(), viewDate.getMonth() + 1, 1);
        renderCalendar();
    });

    clearButton.addEventListener("click", () => {
        input.value = "";
        input.dispatchEvent(new Event("change", { bubbles: true }));
        syncDateLabel();
        renderCalendar();
        closeCustomField(root);
        trigger.focus();
    });

    todayButton.addEventListener("click", () => {
        const today = new Date();

        input.value = toIsoDate(today);
        input.dispatchEvent(new Event("change", { bubbles: true }));
        syncDateLabel();
        viewDate = new Date(today.getFullYear(), today.getMonth(), 1);
        renderCalendar();
        closeCustomField(root);
        trigger.focus();
    });

    syncDateLabel();
    renderCalendar();
});

document.querySelectorAll("[data-phone-field]").forEach((phoneRoot) => {
    const trigger = phoneRoot.querySelector("[data-phone-code-trigger]");
    const flagEl = phoneRoot.querySelector("[data-phone-flag]");
    const codeEl = phoneRoot.querySelector("[data-phone-code]");
    const numberInput = phoneRoot.querySelector("[data-phone-number]");
    const combinedInput = phoneRoot.querySelector("[data-phone-combined]");
    const options = Array.from(phoneRoot.querySelectorAll("[data-phone-option]"));

    const getSelectedOption = () => {
        const selectedCode = codeEl ? codeEl.textContent.trim() : "+44";
        return options.find((option) => option.dataset.code === selectedCode) || options[0] || null;
    };

    const getNationalDigits = () => numberInput ? numberInput.value.replace(/\D/g, "") : "";

    const formatNationalDigits = (digits, pattern) => {
        const groups = (pattern || "").split(/\s+/).map(Number).filter(Boolean);

        if (groups.length === 0) {
            return digits;
        }

        const parts = [];
        let cursor = 0;

        groups.forEach((groupSize) => {
            if (cursor >= digits.length) {
                return;
            }

            parts.push(digits.slice(cursor, cursor + groupSize));
            cursor += groupSize;
        });

        if (cursor < digits.length) {
            parts.push(digits.slice(cursor));
        }

        return parts.filter(Boolean).join(" ");
    };

    const syncPhoneRules = () => {
        if (!numberInput) {
            return;
        }

        const selectedOption = getSelectedOption();
        const maxDigits = Number(selectedOption?.dataset.nationalMax || 15);
        const placeholder = selectedOption?.dataset.placeholder || "7700 900123";
        const pattern = selectedOption?.dataset.nationalFormat || "";
        const digits = getNationalDigits().slice(0, maxDigits);
        const formattedValue = formatNationalDigits(digits, pattern);

        numberInput.placeholder = placeholder;
        numberInput.maxLength = Math.max(placeholder.length, formattedValue.length, maxDigits);
        numberInput.value = formattedValue;
    };

    const updateSelectedState = () => {
        const selectedOption = getSelectedOption();

        options.forEach((option) => {
            const isSelected = option === selectedOption;
            option.classList.toggle("is-selected", isSelected);
            option.setAttribute("aria-selected", String(isSelected));
        });
    };

    const updateCombined = () => {
        syncPhoneRules();
        const code = codeEl ? codeEl.textContent.trim() : "+44";
        const number = numberInput ? numberInput.value.trim() : "";
        if (combinedInput) {
            combinedInput.value = number ? `${code} ${number}` : "";
            combinedInput.dispatchEvent(new Event("input", { bubbles: true }));
        }
    };

    options.forEach((option) => {
        option.addEventListener("click", () => {
            if (flagEl) flagEl.textContent = option.dataset.flag || "";
            if (codeEl) codeEl.textContent = option.dataset.code || "+44";
            updateSelectedState();
            updateCombined();
            closeCustomField(phoneRoot);
            if (numberInput) numberInput.focus();
        });
    });

    if (trigger) {
        trigger.addEventListener("click", () => {
            if (phoneRoot.classList.contains("is-open")) {
                closeCustomField(phoneRoot);
                return;
            }
            openCustomField(phoneRoot);
        });
    }

    if (numberInput) {
        numberInput.addEventListener("input", () => {
            updateCombined();
        });
    }

    // Pre-populate from existing combined value (e.g. after validation error re-render)
    if (combinedInput && combinedInput.value) {
        const existing = combinedInput.value.trim();
        const matched = options.find((opt) => existing.startsWith(opt.dataset.code || ""));
        if (matched) {
            if (flagEl) flagEl.textContent = matched.dataset.flag || "";
            if (codeEl) codeEl.textContent = matched.dataset.code || "+44";
            if (numberInput) numberInput.value = existing.slice((matched.dataset.code || "").length).trim();
        }
    }

    updateSelectedState();
    updateCombined();
});

const contactForm = document.querySelector("[data-contact-form]");

if (contactForm) {
    const fieldOrder = [
        "fullName",
        "email",
        "phone",
        "serviceType",
        "eventDate",
        "venue",
        "message",
        "privacyAccepted"
    ];

    const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const siteHeader = document.querySelector(".site-header");

    const getFieldWrapper = (fieldName) => contactForm.querySelector(`[data-form-field="${fieldName}"]`);

    const getFieldControl = (fieldName) => {
        const wrapper = getFieldWrapper(fieldName);

        if (!wrapper) {
            return null;
        }

        if (fieldName === "privacyAccepted") {
            return wrapper;
        }

        return wrapper.querySelector(".field-control, .form-choice") || wrapper;
    };

    const getErrorNode = (fieldName) => {
        const wrapper = getFieldWrapper(fieldName);

        if (!wrapper) {
            return null;
        }

        if (fieldName === "privacyAccepted") {
            const existing = wrapper.nextElementSibling;

            if (existing && existing.classList.contains("field-error")) {
                return existing;
            }

            const created = document.createElement("p");
            created.className = "field-error";
            created.hidden = true;
            wrapper.insertAdjacentElement("afterend", created);
            return created;
        }

        const existing = wrapper.querySelector(".field-error");

        if (existing) {
            return existing;
        }

        const created = document.createElement("p");
        created.className = "field-error";
        created.hidden = true;
        wrapper.appendChild(created);
        return created;
    };

    const clearFieldError = (fieldName) => {
        const wrapper = getFieldWrapper(fieldName);
        const control = getFieldControl(fieldName);
        const errorNode = getErrorNode(fieldName);

        if (wrapper) {
            wrapper.classList.remove("is-invalid");
        }

        if (control && control !== wrapper) {
            control.classList.remove("is-invalid");
        }

        if (errorNode) {
            errorNode.textContent = "";
            errorNode.hidden = true;
        }
    };

    const setFieldError = (fieldName, message) => {
        const wrapper = getFieldWrapper(fieldName);
        const control = getFieldControl(fieldName);
        const errorNode = getErrorNode(fieldName);

        if (wrapper) {
            wrapper.classList.add("is-invalid");
        }

        if (control && control !== wrapper) {
            control.classList.add("is-invalid");
        }

        if (errorNode) {
            errorNode.textContent = message;
            errorNode.hidden = false;
        }
    };

    const todayAtMidnight = () => {
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        return today;
    };

    const fieldConfig = {
        fullName: {
            input: contactForm.querySelector("#fullName"),
            focusTarget: contactForm.querySelector("#fullName"),
            validate: (value) => value.trim() ? "" : "Please add your full name."
        },
        email: {
            input: contactForm.querySelector("#email"),
            focusTarget: contactForm.querySelector("#email"),
            validate: (value) => {
                const trimmed = value.trim();

                if (!trimmed) {
                    return "Please add an email address.";
                }

                return emailPattern.test(trimmed) ? "" : "Please use a valid email address.";
            }
        },
        phone: {
            input: contactForm.querySelector("#phone"),
            focusTarget: contactForm.querySelector("#phoneNumber"),
            validate: (value) => {
                const trimmed = value.trim();
                if (!trimmed) {
                    return "Please add a phone number.";
                }
                const phoneRoot = contactForm.querySelector("[data-phone-field]");
                const selectedCode = phoneRoot?.querySelector("[data-phone-code]")?.textContent.trim() || "";
                const selectedOption = Array.from(phoneRoot?.querySelectorAll("[data-phone-option]") || [])
                        .find((option) => option.dataset.code === selectedCode);
                const nationalDigits = trimmed.replace(selectedCode, "").replace(/\D/g, "");
                const minDigits = Number(selectedOption?.dataset.nationalMin || 7);
                const maxDigits = Number(selectedOption?.dataset.nationalMax || 15);

                if (nationalDigits.length < minDigits) {
                    return `Please add at least ${minDigits} digits for this country code.`;
                }

                return nationalDigits.length <= maxDigits ? "" : `Please use no more than ${maxDigits} digits for this country code.`;
            }
        },
        serviceType: {
            input: contactForm.querySelector("#serviceType"),
            focusTarget: contactForm.querySelector("[data-choice-trigger]"),
            validate: (value) => value.trim() ? "" : "Please choose the type of ceremony."
        },
        eventDate: {
            input: contactForm.querySelector("#eventDate"),
            focusTarget: contactForm.querySelector("[data-date-trigger]"),
            validate: (value) => {
                if (!value.trim()) {
                    return "Please add a preferred date.";
                }

                const parsed = parseIsoDate(value);

                if (!parsed) {
                    return "Please add a valid preferred date.";
                }

                return parsed < todayAtMidnight() ? "Please choose a date that is today or later." : "";
            }
        },
        venue: {
            input: contactForm.querySelector("#venue"),
            focusTarget: contactForm.querySelector("#venue"),
            validate: (value) => value.trim() ? "" : "Please add the venue or location."
        },
        message: {
            input: contactForm.querySelector("#message"),
            focusTarget: contactForm.querySelector("#message"),
            validate: (value) => {
                const trimmed = value.trim();

                if (!trimmed) {
                    return "Please tell us a little about the ceremony.";
                }

                return trimmed.length >= 20 ? "" : "Please give between 20 and 2000 characters.";
            }
        },
        privacyAccepted: {
            input: contactForm.querySelector("#privacyAccepted"),
            focusTarget: contactForm.querySelector(".checkbox-label"),
            validate: (value, input) => input && input.checked ? "" : "Please confirm that you are happy for us to handle your details."
        }
    };

    const validateField = (fieldName) => {
        const config = fieldConfig[fieldName];

        if (!config || !config.input) {
            return "";
        }

        return config.validate(config.input.value || "", config.input);
    };

    const focusAndScrollToField = (fieldName) => {
        const wrapper = getFieldWrapper(fieldName);
        const focusTarget = fieldConfig[fieldName]?.focusTarget;
        const headerOffset = siteHeader ? siteHeader.offsetHeight : 92;

        if (wrapper) {
            const top = wrapper.getBoundingClientRect().top + window.scrollY - headerOffset - 24;

            window.scrollTo({
                top: Math.max(0, top),
                behavior: "smooth"
            });
        }

        if (focusTarget && typeof focusTarget.focus === "function") {
            window.setTimeout(() => {
                focusTarget.focus({ preventScroll: true });
            }, 180);
        }
    };

    fieldOrder.forEach((fieldName) => {
        const config = fieldConfig[fieldName];

        if (!config?.input) {
            return;
        }

        const syncValidity = () => {
            const errorMessage = validateField(fieldName);

            if (!errorMessage) {
                clearFieldError(fieldName);
                return;
            }

            if (getFieldWrapper(fieldName)?.classList.contains("is-invalid")) {
                setFieldError(fieldName, errorMessage);
            }
        };

        config.input.addEventListener("input", syncValidity);
        config.input.addEventListener("change", syncValidity);
    });

    const privacyRow = getFieldWrapper("privacyAccepted");
    const privacyInput = fieldConfig.privacyAccepted.input;

    if (privacyRow && privacyInput) {
        const syncPrivacyCheckedState = () => {
            privacyRow.classList.toggle("is-checked", privacyInput.checked);
        };

        privacyRow.addEventListener("click", (event) => {
            if (event.target.closest("a") || event.target === privacyInput) {
                return;
            }

            event.preventDefault();
            privacyInput.checked = !privacyInput.checked;
            privacyInput.dispatchEvent(new Event("change", { bubbles: true }));
        });

        privacyInput.addEventListener("change", syncPrivacyCheckedState);
        syncPrivacyCheckedState();
    }

    contactForm.addEventListener("submit", (event) => {
        fieldOrder.forEach((fieldName) => clearFieldError(fieldName));

        const invalidFields = fieldOrder
            .map((fieldName) => ({ fieldName, message: validateField(fieldName) }))
            .filter((entry) => entry.message);

        if (invalidFields.length === 0) {
            return;
        }

        event.preventDefault();

        invalidFields.forEach(({ fieldName, message }) => {
            setFieldError(fieldName, message);
        });

        focusAndScrollToField(invalidFields[0].fieldName);
    });
}
