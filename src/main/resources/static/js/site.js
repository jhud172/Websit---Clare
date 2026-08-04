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
    ".faq-toolbar",
    ".faq-list article",
    ".services-faq-heading",
    ".services-faq-list article",
    ".contact-panel",
    ".contact-form",
    ".cta-panel",
    ".policy-index",
    ".policy-card",
    ".review-story-card",
    ".reviews-hero-proof",
    ".reviews-submit-copy",
    ".reviews-submit-panel"
];

const modalTriggerSelector = "[data-open-enquiry-modal]";
const reducedMotionRequested = () => window.matchMedia("(prefers-reduced-motion: reduce)").matches;

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
    let headerScrollFrame = null;

    const syncHeader = () => {
        headerScrollFrame = null;
        const currentScrollY = window.scrollY;
        const scrollableDistance = Math.max(1, document.documentElement.scrollHeight - window.innerHeight);
        const scrollProgress = Math.min(1, Math.max(0, currentScrollY / scrollableDistance));

        siteHeader.classList.toggle("is-scrolled", currentScrollY > 16);
        document.documentElement.style.setProperty("--page-scroll-progress", scrollProgress.toFixed(4));
    };

    const requestHeaderSync = () => {
        if (headerScrollFrame !== null) {
            return;
        }

        headerScrollFrame = window.requestAnimationFrame(syncHeader);
    };

    siteHeader.classList.remove("is-scroll-up", "is-scroll-down", "is-condensed");
    syncHeader();
    window.addEventListener("scroll", requestHeaderSync, { passive: true });
    window.addEventListener("resize", requestHeaderSync, { passive: true });
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
        if (window.innerWidth > 1240) {
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
    let modalScrollFrame = null;

    const getFocusableElements = () => Array.from(modalDialog.querySelectorAll(
        "a[href], button:not([disabled]), textarea:not([disabled]), input:not([disabled]), select:not([disabled]), [tabindex]:not([tabindex='-1'])"
    )).filter((element) => !element.hasAttribute("hidden"));

    const lockScroll = (locked) => {
        document.body.classList.toggle("is-modal-open", locked);
    };

    const updateModalScrollState = () => {
        if (!modalDialog) {
            return;
        }

        const maxScroll = Math.max(1, modalDialog.scrollHeight - modalDialog.clientHeight);
        const progress = Math.min(1, Math.max(0, modalDialog.scrollTop / maxScroll));
        modalDialog.classList.toggle("is-scrolled", modalDialog.scrollTop > 12);
        modalDialog.classList.toggle("is-at-bottom", progress > 0.96);
    };

    const requestModalScrollState = () => {
        if (modalScrollFrame) {
            return;
        }

        modalScrollFrame = window.requestAnimationFrame(() => {
            modalScrollFrame = null;
            updateModalScrollState();
        });
    };

    const closeModal = () => {
        enquiryModal.hidden = true;
        enquiryModal.classList.remove("is-open");
        modalDialog?.classList.remove("is-scrolled", "is-at-bottom");
        lockScroll(false);
        if (previousFocus && typeof previousFocus.focus === "function") {
            previousFocus.focus({ preventScroll: true });
        }
    };

    const openModal = (trigger) => {
        const openedFromNavigation = Boolean(trigger?.closest("[data-site-nav]"));
        previousFocus = openedFromNavigation ? navToggle : (trigger || document.activeElement);
        if (siteNav?.classList.contains("is-open")) {
            siteNav.classList.remove("is-open");
            navToggle?.setAttribute("aria-expanded", "false");
        }
        enquiryModal.hidden = false;
        enquiryModal.classList.add("is-open");
        lockScroll(true);
        if (modalDialog) {
            modalDialog.scrollTop = 0;
        }
        updateModalScrollState();

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

    modalDialog?.addEventListener("scroll", requestModalScrollState, { passive: true });

    if (enquiryModal.dataset.openOnLoad === "true") {
        window.setTimeout(() => openModal(null), 60);
    }

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
    const track = carouselRoot.querySelector(".review-carousel-track");
    const slides = Array.from(carouselRoot.querySelectorAll("[data-carousel-slide]"));
    const dots = Array.from(carouselRoot.querySelectorAll("[data-carousel-dot]"));
    const prevButton = carouselRoot.querySelector("[data-carousel-prev]");
    const nextButton = carouselRoot.querySelector("[data-carousel-next]");
    const controls = carouselRoot.querySelector(".carousel-controls");
    const isReviewCarousel = carouselRoot.classList.contains("review-carousel");
    const supportsSwipe = isReviewCarousel
        || carouselRoot.classList.contains("about-image-carousel")
        || carouselRoot.classList.contains("about-story-carousel")
        || carouselRoot.classList.contains("services-showcase")
        || carouselRoot.classList.contains("wedding-hero-media")
        || carouselRoot.classList.contains("wedding-editorial-carousel")
        || carouselRoot.classList.contains("funeral-memory-carousel");
    const intervalMs = Number(carouselRoot.dataset.carouselInterval || 6000);
    const transitionMs = Number(carouselRoot.dataset.carouselTransition || 980);
    let currentIndex = slides.findIndex((slide) => slide.classList.contains("is-active"));
    let reviewTrackIndex = currentIndex;
    let timerId = null;
    let transitionTimerId = null;
    let pointerStartX = null;
    let pointerStartY = null;
    let carouselIsVisible = true;
    let carouselIsHovered = false;
    let carouselHasFocus = false;
    let userPaused = false;
    let announceNextChange = false;
    let pauseButton = null;
    let carouselStatus = null;

    if (slides.length === 0) {
        return;
    }

    if (currentIndex < 0) {
        currentIndex = 0;
    }

    const carouselMotionQuery = window.matchMedia("(prefers-reduced-motion: reduce)");
    const prefersReducedMotion = () => carouselMotionQuery.matches;
    const normaliseIndex = (index) => (index + slides.length) % slides.length;

    const hydrateSlideImages = (index) => {
        const slide = slides[normaliseIndex(index)];

        slide?.querySelectorAll("img[data-src]").forEach((image) => {
            const source = image.dataset.src;
            if (!source) {
                return;
            }

            image.src = source;
            image.removeAttribute("data-src");
        });

        slide?.querySelectorAll("source[data-srcset]").forEach((sourceElement) => {
            const sourceSet = sourceElement.dataset.srcset;
            if (!sourceSet) {
                return;
            }

            sourceElement.srcset = sourceSet;
            sourceElement.removeAttribute("data-srcset");
        });
    };

    const hydrateCurrentAndNextSlides = () => {
        hydrateSlideImages(currentIndex);
        if (slides.length > 1) {
            hydrateSlideImages(currentIndex + 1);
        }
    };

    if (carouselRoot.getAttribute("aria-hidden") !== "true") {
        carouselRoot.setAttribute("role", "region");
        carouselRoot.setAttribute("aria-roledescription", "carousel");
        if (!carouselRoot.hasAttribute("aria-label")) {
            carouselRoot.setAttribute("aria-label", controls?.getAttribute("aria-label") || "Image carousel");
        }

        slides.forEach((slide, index) => {
            slide.setAttribute("role", "group");
            slide.setAttribute("aria-roledescription", "slide");
            slide.setAttribute("aria-label", `${index + 1} of ${slides.length}`);
        });
    }

    if (controls && slides.length > 1) {
        pauseButton = document.createElement("button");
        pauseButton.className = "carousel-arrow carousel-play-toggle";
        pauseButton.type = "button";
        pauseButton.dataset.carouselPlayToggle = "";
        controls.appendChild(pauseButton);

        carouselStatus = document.createElement("span");
        carouselStatus.className = "sr-only";
        carouselStatus.setAttribute("role", "status");
        carouselStatus.setAttribute("aria-live", "polite");
        carouselStatus.setAttribute("aria-atomic", "true");
        controls.appendChild(carouselStatus);
    }

    if (prefersReducedMotion()) {
        userPaused = true;
    }

    const updatePauseButton = () => {
        if (!pauseButton) {
            return;
        }

        pauseButton.textContent = userPaused ? "▶" : "Ⅱ";
        pauseButton.setAttribute(
            "aria-label",
            userPaused ? "Play automatic slideshow" : "Pause automatic slideshow"
        );
        pauseButton.title = userPaused ? "Play slideshow" : "Pause slideshow";
    };

    const setReviewTrackPosition = (instant = false) => {
        if (!isReviewCarousel) {
            return;
        }

        if (instant && track) {
            track.style.transitionDuration = "0ms";
        }

        carouselRoot.style.setProperty("--carousel-index", String(reviewTrackIndex));

        if (instant && track) {
            track.getBoundingClientRect();
            window.requestAnimationFrame(() => {
                track.style.removeProperty("transition-duration");
            });
        }
    };

    const renderReviewLoopOrder = (instant = false) => {
        if (!isReviewCarousel || !track) {
            return;
        }

        if (slides.length <= 1) {
            reviewTrackIndex = 0;
            setReviewTrackPosition(instant);
            return;
        }

        const orderedSlides = [];
        for (let offset = -1; offset < slides.length - 1; offset += 1) {
            orderedSlides.push(slides[normaliseIndex(currentIndex + offset)]);
        }

        orderedSlides.forEach((slide) => track.append(slide));
        reviewTrackIndex = 1;
        setReviewTrackPosition(instant);
    };

    const setDots = () => {
        dots.forEach((dot, index) => {
            const active = index === currentIndex;
            dot.classList.toggle("is-active", active);
            dot.setAttribute("aria-current", active ? "true" : "false");
        });
    };

    const setReviewSlideStates = () => {
        if (!isReviewCarousel) {
            return;
        }

        slides.forEach((slide, index) => {
            const previous = normaliseIndex(currentIndex - 1) === index;
            const next = normaliseIndex(currentIndex + 1) === index;
            slide.classList.toggle("is-adjacent", previous || next);
            slide.classList.toggle("is-previous", previous);
            slide.classList.toggle("is-next", next);
        });
    };

    const finishTransition = () => {
        slides.forEach((slide, index) => {
            const active = index === currentIndex;
            slide.classList.toggle("is-active", active);
            slide.classList.remove("is-entering", "is-leaving");
            slide.setAttribute("aria-hidden", String(!active));
            if ("inert" in slide) {
                slide.inert = !active;
            }
        });

        carouselRoot.classList.remove("is-transitioning");
        if (isReviewCarousel) {
            renderReviewLoopOrder(true);
        }
        setReviewSlideStates();
        setDots();

        if (announceNextChange && carouselStatus) {
            carouselStatus.textContent = `Showing slide ${currentIndex + 1} of ${slides.length}.`;
        }
        announceNextChange = false;
    };

    const directionFromIndexes = (nextIndex) => {
        const normalisedNext = normaliseIndex(nextIndex);
        const forwardDistance = (normalisedNext - currentIndex + slides.length) % slides.length;
        const backwardDistance = (currentIndex - normalisedNext + slides.length) % slides.length;
        return forwardDistance <= backwardDistance ? "next" : "previous";
    };

    const moveTo = (nextIndex, requestedDirection, announce = false) => {
        if (carouselRoot.classList.contains("is-transitioning")) {
            return;
        }

        const normalisedNext = normaliseIndex(nextIndex);

        if (normalisedNext === currentIndex) {
            return;
        }

        const previousIndex = currentIndex;
        const direction = requestedDirection || directionFromIndexes(normalisedNext);
        hydrateSlideImages(normalisedNext);
        hydrateSlideImages(normalisedNext + (direction === "previous" ? -1 : 1));
        currentIndex = normalisedNext;
        announceNextChange = announce;
        carouselRoot.dataset.carouselDirection = direction;

        if (isReviewCarousel && slides.length > 1) {
            const isAdjacentNext = normaliseIndex(previousIndex + 1) === normalisedNext;
            const isAdjacentPrevious = normaliseIndex(previousIndex - 1) === normalisedNext;

            if (direction === "next" && isAdjacentNext) {
                reviewTrackIndex = 2;
            }
            else if (direction === "previous" && isAdjacentPrevious) {
                reviewTrackIndex = 0;
            }
            else {
                renderReviewLoopOrder(true);
                finishTransition();
                return;
            }
        }

        if (transitionTimerId) {
            window.clearTimeout(transitionTimerId);
        }

        setDots();

        if (prefersReducedMotion()) {
            finishTransition();
            return;
        }

        slides.forEach((slide, index) => {
            const entering = index === currentIndex;
            const leaving = index === previousIndex;
            slide.classList.toggle("is-active", isReviewCarousel ? entering : entering || leaving);
            slide.classList.toggle("is-entering", entering);
            slide.classList.toggle("is-leaving", leaving);
            slide.setAttribute("aria-hidden", String(!entering));
            if ("inert" in slide) {
                slide.inert = !entering;
            }
        });

        carouselRoot.classList.add("is-transitioning");
        setReviewTrackPosition();
        setReviewSlideStates();
        transitionTimerId = window.setTimeout(finishTransition, transitionMs);
    };

    const restartTimer = () => {
        if (timerId) {
            window.clearInterval(timerId);
            timerId = null;
        }

        if (userPaused || !carouselIsVisible || carouselIsHovered || carouselHasFocus) {
            updatePauseButton();
            return;
        }

        timerId = window.setInterval(() => {
            moveTo(currentIndex + 1, "next");
        }, intervalMs);
        updatePauseButton();
    };

    const stopTimer = () => {
        if (timerId) {
            window.clearInterval(timerId);
            timerId = null;
        }
    };

    pauseButton?.addEventListener("click", () => {
        userPaused = !userPaused;
        if (userPaused) {
            stopTimer();
            if (carouselStatus) {
                carouselStatus.textContent = "Automatic slideshow paused.";
            }
        }
        else {
            restartTimer();
            if (carouselStatus) {
                carouselStatus.textContent = carouselHasFocus
                    ? "Automatic slideshow will play when focus leaves the carousel."
                    : "Automatic slideshow playing.";
            }
        }
        updatePauseButton();
    });

    if ("IntersectionObserver" in window) {
        const carouselObserver = new IntersectionObserver((entries) => {
            entries.forEach((entry) => {
                carouselIsVisible = entry.isIntersecting;

                if (carouselIsVisible) {
                    restartTimer();
                }
                else {
                    stopTimer();
                }
            });
        }, {
            rootMargin: "160px 0px",
            threshold: 0.01
        });

        carouselObserver.observe(carouselRoot);
    }

    prevButton?.addEventListener("click", () => {
        moveTo(currentIndex - 1, "previous", true);
        restartTimer();
    });

    nextButton?.addEventListener("click", () => {
        moveTo(currentIndex + 1, "next", true);
        restartTimer();
    });

    dots.forEach((dot, index) => {
        dot.addEventListener("click", () => {
            moveTo(index, directionFromIndexes(index), true);
            restartTimer();
        });
    });

    carouselRoot.addEventListener("mouseenter", () => {
        carouselIsHovered = true;
        stopTimer();
    });

    carouselRoot.addEventListener("mouseleave", () => {
        carouselIsHovered = false;
        restartTimer();
    });

    carouselRoot.addEventListener("focusin", () => {
        carouselHasFocus = true;
        stopTimer();
    });

    carouselRoot.addEventListener("focusout", () => {
        window.setTimeout(() => {
            carouselHasFocus = carouselRoot.contains(document.activeElement);
            if (!carouselHasFocus) {
                restartTimer();
            }
        }, 0);
    });

    if (supportsSwipe) {
        carouselRoot.addEventListener("pointerdown", (event) => {
            pointerStartX = event.clientX;
            pointerStartY = event.clientY;
            carouselRoot.classList.add("is-dragging");
        });

        carouselRoot.addEventListener("pointerup", (event) => {
            if (pointerStartX === null || pointerStartY === null) {
                return;
            }

            const deltaX = event.clientX - pointerStartX;
            const deltaY = event.clientY - pointerStartY;
            pointerStartX = null;
            pointerStartY = null;
            carouselRoot.classList.remove("is-dragging");

            if (Math.abs(deltaX) < 42 || Math.abs(deltaX) < Math.abs(deltaY)) {
                return;
            }

            moveTo(
                currentIndex + (deltaX < 0 ? 1 : -1),
                deltaX < 0 ? "next" : "previous",
                true
            );
            restartTimer();
        });

        carouselRoot.addEventListener("pointercancel", () => {
            pointerStartX = null;
            pointerStartY = null;
            carouselRoot.classList.remove("is-dragging");
        });
    }

    carouselMotionQuery.addEventListener?.("change", (event) => {
        if (!event.matches) {
            return;
        }

        userPaused = true;
        stopTimer();
        if (transitionTimerId) {
            window.clearTimeout(transitionTimerId);
            transitionTimerId = null;
            finishTransition();
        }
        if (carouselStatus) {
            carouselStatus.textContent = "Automatic slideshow paused to respect reduced motion settings.";
        }
        updatePauseButton();
    });

    carouselRoot.dataset.carouselDirection = "next";
    updatePauseButton();
    finishTransition();
    restartTimer();
    window.requestAnimationFrame(hydrateCurrentAndNextSlides);
});

const protectImage = (image) => {
    if (!(image instanceof HTMLImageElement) || image.dataset.imageProtected === "true") {
        return;
    }

    image.dataset.imageProtected = "true";
    if (!image.hasAttribute("decoding")) {
        image.decoding = "async";
    }
    image.setAttribute("draggable", "false");
    image.setAttribute("oncontextmenu", "return false;");

    image.addEventListener("dragstart", (event) => {
        event.preventDefault();
    });

    image.addEventListener("contextmenu", (event) => {
        event.preventDefault();
    });
};

document.querySelectorAll("img").forEach(protectImage);

document.addEventListener("dragstart", (event) => {
    if (event.target instanceof Element && event.target.closest("img")) {
        event.preventDefault();
    }
}, true);

document.addEventListener("contextmenu", (event) => {
    if (event.target instanceof Element && event.target.closest("img")) {
        event.preventDefault();
    }
}, true);

document.addEventListener("copy", (event) => {
    const selection = window.getSelection();

    if (!selection || selection.isCollapsed) {
        return;
    }

    const container = document.createElement("div");

    for (let index = 0; index < selection.rangeCount; index += 1) {
        container.appendChild(selection.getRangeAt(index).cloneContents());
    }

    if (container.querySelector("img")) {
        event.preventDefault();
    }
});

const imageProtectionObserver = new MutationObserver((mutations) => {
    mutations.forEach((mutation) => {
        mutation.addedNodes.forEach((node) => {
            if (!(node instanceof Element)) {
                return;
            }

            if (node instanceof HTMLImageElement) {
                protectImage(node);
            }

            node.querySelectorAll("img").forEach(protectImage);
        });
    });
});

imageProtectionObserver.observe(document.documentElement, {
    childList: true,
    subtree: true
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
    const clearRevealMotionState = (element) => {
        const finish = (event) => {
            if (event && event.target !== element) {
                return;
            }

            element.removeEventListener("transitionend", finish);
            element.classList.remove("reveal-ready", "reveal-delay-1", "reveal-delay-2", "reveal-delay-3");
        };

        element.addEventListener("transitionend", finish);
        window.setTimeout(() => finish(), 1100);
    };

    revealTargets.forEach((element) => {
        element.classList.add("reveal-ready");
    });

    const revealObserver = new IntersectionObserver((entries, observer) => {
        entries.forEach((entry) => {
            if (!entry.isIntersecting) {
                return;
            }

            entry.target.classList.add("is-visible");
            clearRevealMotionState(entry.target);
            observer.unobserve(entry.target);
        });
    }, {
        threshold: 0.08,
        rootMargin: "0px 0px 10% 0px"
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

const getAcceptedFileMatchers = (input) => (input.accept || "")
    .split(",")
    .map((value) => value.trim().toLowerCase())
    .filter(Boolean);

const acceptsFile = (input, file) => {
    const matchers = getAcceptedFileMatchers(input);

    if (matchers.length === 0) {
        return true;
    }

    const fileName = file.name.toLowerCase();
    const fileType = file.type.toLowerCase();

    return matchers.some((matcher) => {
        if (matcher.startsWith(".")) {
            return fileName.endsWith(matcher);
        }

        if (matcher.endsWith("/*")) {
            return fileType.startsWith(matcher.slice(0, -1));
        }

        return fileType === matcher;
    });
};

fileInputs.forEach((input) => {
    const targetId = input.getAttribute("data-file-list-target");
    const fileList = targetId ? document.getElementById(targetId) : null;
    const dropzone = input.closest("[data-file-dropzone]");
    const fieldGroup = input.closest(".field-group");
    const maxFileCount = input.id === "reviewPhotos" ? 10 : 3;
    const maxFileSize = 5 * 1024 * 1024;
    let fileError = fieldGroup?.querySelector("[data-file-error]") || null;
    let previewUrls = [];

    if (!fileError && fieldGroup) {
        fileError = document.createElement("p");
        fileError.className = "field-error";
        fileError.dataset.fileError = "";
        fileError.hidden = true;
        (fileList || dropzone)?.insertAdjacentElement("afterend", fileError);
    }

    if (fileError) {
        fileError.id = fileError.id || `${input.id || "file-upload"}-error`;
        fileError.setAttribute("role", "alert");
        fileError.setAttribute("aria-live", "polite");
        fileError.setAttribute("aria-atomic", "true");
        const describedBy = new Set((input.getAttribute("aria-describedby") || "").split(/\s+/).filter(Boolean));
        describedBy.add(fileError.id);
        input.setAttribute("aria-describedby", Array.from(describedBy).join(" "));
    }

    const clearPreviewUrls = () => {
        previewUrls.forEach((url) => URL.revokeObjectURL(url));
        previewUrls = [];
    };

    const replaceInputFiles = (files) => {
        const dataTransfer = new DataTransfer();
        files.forEach((file) => dataTransfer.items.add(file));
        input.files = dataTransfer.files;
    };

    const setFileError = (message) => {
        input.setAttribute("aria-invalid", String(Boolean(message)));
        if (message && fileError?.id) {
            input.setAttribute("aria-errormessage", fileError.id);
        }
        else {
            input.removeAttribute("aria-errormessage");
        }
        if (!fileError) {
            return;
        }

        fileError.textContent = message;
        fileError.hidden = !message;
    };

    const validateSelectedFiles = (files) => {
        const rejectedTypes = files.filter((file) => !acceptsFile(input, file));
        const acceptedTypes = files.filter((file) => acceptsFile(input, file));
        const oversizedFiles = acceptedTypes.filter((file) => file.size > maxFileSize);
        const validFiles = acceptedTypes.filter((file) => file.size <= maxFileSize);
        const keptFiles = validFiles.slice(0, input.multiple ? maxFileCount : 1);
        const messages = [];

        if (rejectedTypes.length > 0) {
            messages.push(`${rejectedTypes.length} unsupported file${rejectedTypes.length === 1 ? " was" : "s were"} not added.`);
        }
        if (oversizedFiles.length > 0) {
            messages.push(`${oversizedFiles.length} file${oversizedFiles.length === 1 ? " is" : "s are"} larger than 5 MB and ${oversizedFiles.length === 1 ? "was" : "were"} not added.`);
        }
        if (validFiles.length > keptFiles.length) {
            messages.push(`Please choose no more than ${input.multiple ? maxFileCount : 1} file${input.multiple && maxFileCount !== 1 ? "s" : ""}.`);
        }

        return { files: keptFiles, message: messages.join(" ") };
    };

    const syncFiles = ({ validate = true } = {}) => {
        if (!dropzone || !fileList) {
            return;
        }

        const incomingFiles = Array.from(input.files || []);
        const result = validate ? validateSelectedFiles(incomingFiles) : { files: incomingFiles, message: "" };
        const filesChanged = result.files.length !== incomingFiles.length
            || result.files.some((file, index) => file !== incomingFiles[index]);

        if (filesChanged) {
            replaceInputFiles(result.files);
        }
        if (validate) {
            setFileError(result.message);
        }

        const files = Array.from(input.files || []);
        clearPreviewUrls();
        dropzone.classList.toggle("has-files", files.length > 0);
        fileList.innerHTML = "";
        fileList.hidden = files.length === 0;

        files.forEach((file) => {
            const item = document.createElement("li");
            item.className = "file-list-item";

            if (file.type.startsWith("image/")) {
                const image = document.createElement("img");
                const previewUrl = URL.createObjectURL(file);
                previewUrls.push(previewUrl);
                image.className = "file-preview-image";
                image.src = previewUrl;
                image.alt = "";
                image.loading = "lazy";
                item.appendChild(image);
            }
            else {
                const icon = document.createElement("span");
                const extension = file.name.includes(".") ? file.name.split(".").pop().slice(0, 4).toUpperCase() : "FILE";
                icon.className = "file-preview-icon";
                icon.textContent = extension;
                item.appendChild(icon);
            }

            const meta = document.createElement("span");
            meta.className = "file-preview-meta";

            const name = document.createElement("span");
            name.className = "file-preview-name";
            name.textContent = file.name;

            const size = document.createElement("span");
            size.className = "file-preview-size";
            size.textContent = formatFileSize(file.size);

            meta.append(name, size);
            item.appendChild(meta);
            fileList.appendChild(item);
        });
    };

    const setDroppedFiles = (files) => {
        const droppedFiles = Array.from(files || []);

        if (droppedFiles.length === 0) {
            dropzone?.classList.remove("is-drag-over");
            return;
        }

        replaceInputFiles(droppedFiles);
        input.dispatchEvent(new Event("change", { bubbles: true }));
    };

    const initialFileError = fileError && !fileError.hidden ? fileError.textContent.trim() : "";
    setFileError(initialFileError);
    syncFiles({ validate: false });
    input.addEventListener("change", () => syncFiles({ validate: true }));

    if (dropzone) {
        ["dragenter", "dragover"].forEach((eventName) => {
            dropzone.addEventListener(eventName, (event) => {
                event.preventDefault();
                event.stopPropagation();
                dropzone.classList.add("is-drag-over");
            });
        });

        ["dragleave", "dragend"].forEach((eventName) => {
            dropzone.addEventListener(eventName, (event) => {
                if (eventName === "dragleave" && dropzone.contains(event.relatedTarget)) {
                    return;
                }

                dropzone.classList.remove("is-drag-over");
            });
        });

        dropzone.addEventListener("drop", (event) => {
            event.preventDefault();
            event.stopPropagation();
            dropzone.classList.remove("is-drag-over");
            setDroppedFiles(event.dataTransfer?.files);
        });
    }
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

const formatNamePart = (part) => part
    .split("-")
    .map((segment) => segment
        .split("'")
        .map((piece) => piece ? piece.charAt(0).toUpperCase() + piece.slice(1).toLowerCase() : piece)
        .join("'"))
    .join("-");

const formatPersonName = (value) => value
    .trim()
    .replace(/\s+/g, " ")
    .split(" ")
    .map(formatNamePart)
    .join(" ");

const attachNameAutoFormat = (input) => {
    if (!input) {
        return;
    }

    const apply = () => {
        const formatted = formatPersonName(input.value || "");

        if (formatted && formatted !== input.value) {
            input.value = formatted;
            input.dispatchEvent(new Event("input", { bubbles: true }));
        }
    };

    input.addEventListener("blur", apply);
    input.addEventListener("change", apply);
};

const formatMonthLabel = (date) => new Intl.DateTimeFormat("en-GB", {
    month: "long",
    year: "numeric"
}).format(date);

const customFieldRoots = Array.from(document.querySelectorAll("[data-choice-select], [data-date-picker], [data-phone-field]"));
let customFieldId = 0;

const ensureElementId = (element, prefix) => {
    if (!element) {
        return "";
    }

    if (!element.id) {
        customFieldId += 1;
        element.id = `${prefix}-${customFieldId}`;
    }

    return element.id;
};

const getCustomFieldLabel = (root, input) => {
    const inputId = input?.id;
    return (inputId ? root.querySelector(`label[for="${inputId}"]`) : null)
        || root.querySelector("label, .field-label");
};

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

customFieldRoots.forEach((root) => {
    root.addEventListener("keydown", (event) => {
        if (event.key !== "Escape" || !root.classList.contains("is-open")) {
            return;
        }

        event.preventDefault();
        event.stopPropagation();
        closeCustomField(root);
        root.querySelector("[data-choice-trigger], [data-date-trigger], [data-phone-code-trigger]")?.focus();
    });

    root.addEventListener("focusout", () => {
        window.setTimeout(() => {
            if (!root.contains(document.activeElement)) {
                closeCustomField(root);
            }
        }, 0);
    });
});

const enableOptionKeyboardNavigation = (root, trigger, options) => {
    let typeAhead = "";
    let typeAheadTimer = null;

    const focusOption = (index) => {
        if (options.length === 0) {
            return;
        }

        const normalisedIndex = (index + options.length) % options.length;
        options[normalisedIndex].focus();
    };

    const focusMatchingOption = (character) => {
        window.clearTimeout(typeAheadTimer);
        typeAhead += character.toLowerCase();
        typeAheadTimer = window.setTimeout(() => {
            typeAhead = "";
        }, 650);

        const currentIndex = Math.max(0, options.indexOf(document.activeElement));
        const orderedOptions = [...options.slice(currentIndex + 1), ...options.slice(0, currentIndex + 1)];
        const match = orderedOptions.find((option) => option.textContent.trim().toLowerCase().startsWith(typeAhead));
        match?.focus();
    };

    options.forEach((option) => {
        option.tabIndex = -1;
        option.addEventListener("keydown", (event) => {
            const currentIndex = options.indexOf(option);

            if (event.key === "ArrowDown" || event.key === "ArrowRight") {
                event.preventDefault();
                focusOption(currentIndex + 1);
            }
            else if (event.key === "ArrowUp" || event.key === "ArrowLeft") {
                event.preventDefault();
                focusOption(currentIndex - 1);
            }
            else if (event.key === "Home") {
                event.preventDefault();
                focusOption(0);
            }
            else if (event.key === "End") {
                event.preventDefault();
                focusOption(options.length - 1);
            }
            else if (event.key.length === 1 && /[\p{L}\p{N}]/u.test(event.key)) {
                focusMatchingOption(event.key);
            }
        });
    });

    trigger.addEventListener("keydown", (event) => {
        if (!["ArrowDown", "ArrowUp", "Home", "End"].includes(event.key)) {
            return;
        }

        event.preventDefault();
        openCustomField(root);
        const selectedIndex = options.findIndex((option) => option.getAttribute("aria-selected") === "true");
        const targetIndex = event.key === "ArrowUp" || event.key === "End"
            ? (event.key === "End" ? options.length - 1 : (selectedIndex >= 0 ? selectedIndex : options.length - 1))
            : (event.key === "Home" ? 0 : (selectedIndex >= 0 ? selectedIndex : 0));
        window.requestAnimationFrame(() => focusOption(targetIndex));
    });

    trigger.addEventListener("click", (event) => {
        if (event.detail !== 0) {
            return;
        }

        const selectedIndex = options.findIndex((option) => option.getAttribute("aria-selected") === "true");
        window.requestAnimationFrame(() => {
            if (root.classList.contains("is-open")) {
                focusOption(selectedIndex >= 0 ? selectedIndex : 0);
            }
        });
    });
};

document.querySelectorAll("[data-choice-select]").forEach((root) => {
    const input = root.querySelector("[data-choice-input]");
    const trigger = root.querySelector("[data-choice-trigger]");
    const label = root.querySelector("[data-choice-label]");
    const options = Array.from(root.querySelectorAll("[data-choice-option]"));
    const placeholder = trigger?.dataset.choicePlaceholder || "";

    if (!input || !trigger || !label || options.length === 0) {
        return;
    }

    const fieldLabel = getCustomFieldLabel(root, input);
    const listbox = root.querySelector('[role="listbox"]');
    const fieldLabelId = ensureElementId(fieldLabel, "choice-label");
    const valueLabelId = ensureElementId(label, "choice-value");
    const listboxId = ensureElementId(listbox, "choice-listbox");
    const fieldName = input.name || input.id || root.dataset.formField || root.dataset.reviewField || "";

    if (fieldLabelId && valueLabelId) {
        trigger.setAttribute("aria-labelledby", `${fieldLabelId} ${valueLabelId}`);
    }
    if (listboxId) {
        trigger.setAttribute("aria-controls", listboxId);
    }
    if (fieldLabelId && listbox) {
        listbox.setAttribute("aria-labelledby", fieldLabelId);
        listbox.querySelector(".field-option-placeholder")?.setAttribute("aria-hidden", "true");
        listbox.querySelectorAll("ul").forEach((list) => list.setAttribute("role", "presentation"));
        listbox.querySelectorAll("li").forEach((item) => item.setAttribute("role", "presentation"));
    }
    if (["serviceType", "ceremonyType", "rating"].includes(fieldName)) {
        trigger.setAttribute("aria-required", "true");
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

    enableOptionKeyboardNavigation(root, trigger, options);
    input.addEventListener("change", syncChoice);
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

    const fieldLabel = getCustomFieldLabel(root, input);
    const dateDialog = root.querySelector(".date-picker-shell");
    const fieldLabelId = ensureElementId(fieldLabel, "date-label");
    const valueLabelId = ensureElementId(label, "date-value");
    const monthLabelId = ensureElementId(monthLabel, "date-month");
    const dateDialogId = ensureElementId(dateDialog, "date-dialog");

    if (fieldLabelId && valueLabelId) {
        trigger.setAttribute("aria-labelledby", `${fieldLabelId} ${valueLabelId}`);
    }
    if (dateDialogId) {
        trigger.setAttribute("aria-controls", dateDialogId);
    }
    if (fieldLabelId && monthLabelId) {
        grid.setAttribute("aria-labelledby", `${fieldLabelId} ${monthLabelId}`);
        dateDialog?.setAttribute("aria-labelledby", `${fieldLabelId} ${monthLabelId}`);
    }
    grid.setAttribute("role", "group");
    dateDialog?.setAttribute("role", "dialog");
    monthLabel.setAttribute("aria-live", "polite");
    if (root.dataset.formField === "eventDate") {
        trigger.setAttribute("aria-required", "true");
    }

    let selectedDate = parseIsoDate(input.value);
    let viewDate = selectedDate ? new Date(selectedDate.getFullYear(), selectedDate.getMonth(), 1) : new Date(new Date().getFullYear(), new Date().getMonth(), 1);

    const formatAccessibleDate = (date) => new Intl.DateTimeFormat("en-GB", {
        weekday: "long",
        day: "numeric",
        month: "long",
        year: "numeric"
    }).format(date);

    const syncDateLabel = () => {
        selectedDate = parseIsoDate(input.value);
        label.textContent = selectedDate ? formatDisplayDate(selectedDate) : placeholder;
        trigger.classList.toggle("is-placeholder", !selectedDate);
    };

    const renderCalendar = (focusDateValue = "") => {
        monthLabel.textContent = formatMonthLabel(viewDate);
        grid.innerHTML = "";

        const today = new Date();
        const monthStart = new Date(viewDate.getFullYear(), viewDate.getMonth(), 1);
        const weekdayOffset = (monthStart.getDay() + 6) % 7;
        const firstVisibleDate = new Date(viewDate.getFullYear(), viewDate.getMonth(), 1 - weekdayOffset);
        const preferredFocusDate = focusDateValue
            || (selectedDate && selectedDate.getMonth() === viewDate.getMonth() && selectedDate.getFullYear() === viewDate.getFullYear()
                ? toIsoDate(selectedDate)
                : (today.getMonth() === viewDate.getMonth() && today.getFullYear() === viewDate.getFullYear()
                    ? toIsoDate(today)
                    : toIsoDate(monthStart)));

        const focusCalendarDate = (date) => {
            viewDate = new Date(date.getFullYear(), date.getMonth(), 1);
            const targetValue = toIsoDate(date);
            renderCalendar(targetValue);
            window.requestAnimationFrame(() => {
                grid.querySelector(`[data-date-value="${targetValue}"]`)?.focus();
            });
        };

        for (let index = 0; index < 42; index += 1) {
            const date = new Date(firstVisibleDate.getFullYear(), firstVisibleDate.getMonth(), firstVisibleDate.getDate() + index);
            const dayButton = document.createElement("button");

            dayButton.type = "button";
            dayButton.className = "calendar-day";
            dayButton.textContent = String(date.getDate());
            dayButton.dataset.dateValue = toIsoDate(date);
            dayButton.setAttribute("aria-label", formatAccessibleDate(date));
            dayButton.setAttribute("aria-pressed", String(Boolean(selectedDate && isSameDay(date, selectedDate))));
            dayButton.tabIndex = dayButton.dataset.dateValue === preferredFocusDate ? 0 : -1;

            if (date.getMonth() !== viewDate.getMonth()) {
                dayButton.classList.add("is-outside-month");
            }

            if (isSameDay(date, today)) {
                dayButton.classList.add("is-today");
                dayButton.setAttribute("aria-current", "date");
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

            dayButton.addEventListener("keydown", (event) => {
                let targetDate = null;

                if (event.key === "ArrowLeft") {
                    targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - 1);
                }
                else if (event.key === "ArrowRight") {
                    targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() + 1);
                }
                else if (event.key === "ArrowUp") {
                    targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - 7);
                }
                else if (event.key === "ArrowDown") {
                    targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() + 7);
                }
                else if (event.key === "Home") {
                    const mondayOffset = (date.getDay() + 6) % 7;
                    targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() - mondayOffset);
                }
                else if (event.key === "End") {
                    const sundayOffset = 6 - ((date.getDay() + 6) % 7);
                    targetDate = new Date(date.getFullYear(), date.getMonth(), date.getDate() + sundayOffset);
                }
                else if (event.key === "PageUp" || event.key === "PageDown") {
                    const monthDelta = event.key === "PageUp" ? -1 : 1;
                    const yearDelta = event.shiftKey ? monthDelta : 0;
                    const targetMonth = event.shiftKey ? date.getMonth() : date.getMonth() + monthDelta;
                    const targetYear = date.getFullYear() + yearDelta;
                    const daysInTargetMonth = new Date(targetYear, targetMonth + 1, 0).getDate();
                    targetDate = new Date(targetYear, targetMonth, Math.min(date.getDate(), daysInTargetMonth));
                }

                if (targetDate) {
                    event.preventDefault();
                    focusCalendarDate(targetDate);
                }
            });

            grid.appendChild(dayButton);
        }
    };

    const openDatePicker = (focusGrid = false) => {
        const currentValue = parseIsoDate(input.value);
        const today = new Date();
        viewDate = currentValue
            ? new Date(currentValue.getFullYear(), currentValue.getMonth(), 1)
            : new Date(today.getFullYear(), today.getMonth(), 1);
        const focusValue = focusGrid ? toIsoDate(currentValue || today) : "";
        renderCalendar(focusValue);
        openCustomField(root);

        if (focusGrid) {
            window.requestAnimationFrame(() => {
                grid.querySelector(`[data-date-value="${focusValue}"]`)?.focus();
            });
        }
    };

    trigger.addEventListener("click", (event) => {
        if (root.classList.contains("is-open")) {
            closeCustomField(root);
            return;
        }

        openDatePicker(event.detail === 0);
    });

    trigger.addEventListener("keydown", (event) => {
        if (event.key !== "ArrowDown" && event.key !== "ArrowUp") {
            return;
        }

        event.preventDefault();
        openDatePicker(true);
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

    input.addEventListener("change", syncDateLabel);
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
    const listbox = phoneRoot.querySelector('[role="listbox"]');

    if (trigger && listbox) {
        const listboxId = ensureElementId(listbox, "phone-listbox");
        trigger.setAttribute("aria-controls", listboxId);
        listbox.querySelectorAll("li").forEach((item) => item.setAttribute("role", "presentation"));
        enableOptionKeyboardNavigation(phoneRoot, trigger, options);
    }

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

        if (trigger && selectedOption) {
            trigger.setAttribute("aria-label", `Country code: ${selectedOption.textContent.trim()}`);
        }
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

    const prepareErrorNode = (fieldName, errorNode) => {
        if (!errorNode) {
            return null;
        }

        if (!errorNode.id) {
            errorNode.id = `${fieldName}-error`;
        }
        errorNode.setAttribute("role", "alert");
        errorNode.setAttribute("aria-live", "polite");
        errorNode.setAttribute("aria-atomic", "true");
        return errorNode;
    };

    const getErrorNode = (fieldName) => {
        const wrapper = getFieldWrapper(fieldName);

        if (!wrapper) {
            return null;
        }

        if (fieldName === "privacyAccepted") {
            const existing = wrapper.nextElementSibling;

            if (existing && existing.classList.contains("field-error")) {
                return prepareErrorNode(fieldName, existing);
            }

            const created = document.createElement("p");
            created.className = "field-error";
            created.hidden = true;
            wrapper.insertAdjacentElement("afterend", created);
            return prepareErrorNode(fieldName, created);
        }

        const existing = wrapper.querySelector(".field-error");

        if (existing) {
            return prepareErrorNode(fieldName, existing);
        }

        const created = document.createElement("p");
        created.className = "field-error";
        created.hidden = true;
        wrapper.appendChild(created);
        return prepareErrorNode(fieldName, created);
    };

    const setFieldAccessibility = (fieldName, invalid, errorNode) => {
        const config = fieldConfig[fieldName];
        const targets = Array.from(new Set([config?.input, config?.focusTarget].filter(Boolean)));

        targets.forEach((target) => {
            target.setAttribute("aria-invalid", String(invalid));

            if (!errorNode?.id) {
                return;
            }

            const describedBy = new Set((target.getAttribute("aria-describedby") || "").split(/\s+/).filter(Boolean));
            describedBy.add(errorNode.id);
            target.setAttribute("aria-describedby", Array.from(describedBy).join(" "));

            if (invalid) {
                target.setAttribute("aria-errormessage", errorNode.id);
            }
            else {
                target.removeAttribute("aria-errormessage");
            }
        });
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
        setFieldAccessibility(fieldName, false, errorNode);
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
            if (errorNode.textContent !== message) {
                errorNode.textContent = message;
            }
            errorNode.hidden = false;
        }
        setFieldAccessibility(fieldName, true, errorNode);
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
            focusTarget: contactForm.querySelector("#privacyAccepted"),
            validate: (value, input) => input && input.checked ? "" : "Please confirm that you are happy for us to handle your details."
        }
    };

    attachNameAutoFormat(fieldConfig.fullName.input);

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
        const modalDialog = contactForm.closest("[data-enquiry-modal-dialog]");
        const headerOffset = siteHeader ? siteHeader.offsetHeight : 92;

        if (wrapper) {
            if (modalDialog) {
                const wrapperTop = wrapper.getBoundingClientRect().top;
                const dialogTop = modalDialog.getBoundingClientRect().top;
                const top = modalDialog.scrollTop + wrapperTop - dialogTop - 24;

                modalDialog.scrollTo({
                    top: Math.max(0, top),
                    behavior: reducedMotionRequested() ? "auto" : "smooth"
                });
            }
            else {
                const top = wrapper.getBoundingClientRect().top + window.scrollY - headerOffset - 24;

                window.scrollTo({
                    top: Math.max(0, top),
                    behavior: reducedMotionRequested() ? "auto" : "smooth"
                });
            }
        }

        if (focusTarget && typeof focusTarget.focus === "function") {
            window.setTimeout(() => {
                focusTarget.focus({ preventScroll: true });
            }, reducedMotionRequested() ? 0 : 180);
        }
    };

    fieldOrder.forEach((fieldName) => {
        const config = fieldConfig[fieldName];

        if (!config?.input) {
            return;
        }

        const errorNode = getErrorNode(fieldName);
        const hasServerError = Boolean(errorNode && !errorNode.hidden && errorNode.textContent.trim());
        setFieldAccessibility(fieldName, hasServerError, errorNode);

        const syncValidity = () => {
            const errorMessage = validateField(fieldName);

            if (!errorMessage) {
                clearFieldError(fieldName);
                return;
            }

            const control = getFieldControl(fieldName);
            const visibleError = getErrorNode(fieldName);
            if (getFieldWrapper(fieldName)?.classList.contains("is-invalid")
                    || control?.classList.contains("is-invalid")
                    || (visibleError && !visibleError.hidden)) {
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

const reviewForm = document.querySelector("[data-review-form]");

if (reviewForm) {
    const successBanner = document.querySelector("[data-review-form-success]");
    const errorBanner = document.querySelector("[data-review-form-error]");
    const submitButton = reviewForm.querySelector('button[type="submit"]');
    const reviewFieldOrder = [
        "reviewerName",
        "reviewerRole",
        "ceremonyType",
        "rating",
        "headline",
        "message",
        "eventDate",
        "reviewPhotos",
        "consentAccepted"
    ];

    const getReviewFieldWrapper = (fieldName) => reviewForm.querySelector(`[data-review-field="${fieldName}"]`);

    const getReviewFieldControl = (fieldName) => {
        const wrapper = getReviewFieldWrapper(fieldName);

        if (!wrapper) {
            return null;
        }

        if (fieldName === "consentAccepted") {
            return wrapper;
        }

        return wrapper.querySelector(".field-control, .field-control-button, .file-dropzone") || wrapper;
    };

    const getReviewFieldFocusTarget = (fieldName) => {
        const wrapper = getReviewFieldWrapper(fieldName);

        if (!wrapper) {
            return null;
        }

        if (fieldName === "consentAccepted") {
            return wrapper.querySelector("#consentAccepted");
        }
        if (fieldName === "reviewPhotos") {
            return wrapper.querySelector("#reviewPhotos");
        }

        return wrapper.querySelector("[data-choice-trigger], [data-date-trigger], input:not([type='hidden']), textarea, button") || null;
    };

    const prepareReviewErrorNode = (fieldName, errorNode) => {
        if (!errorNode) {
            return null;
        }

        if (!errorNode.id) {
            errorNode.id = `review-${fieldName}-error`;
        }
        errorNode.setAttribute("role", "alert");
        errorNode.setAttribute("aria-live", "polite");
        errorNode.setAttribute("aria-atomic", "true");
        return errorNode;
    };

    const getReviewFieldError = (fieldName) => {
        const wrapper = getReviewFieldWrapper(fieldName);

        if (!wrapper) {
            return null;
        }

        if (fieldName === "consentAccepted") {
            const existing = wrapper.nextElementSibling;

            if (existing?.classList.contains("field-error")) {
                return prepareReviewErrorNode(fieldName, existing);
            }

            const created = document.createElement("p");
            created.className = "field-error";
            created.hidden = true;
            wrapper.insertAdjacentElement("afterend", created);
            return prepareReviewErrorNode(fieldName, created);
        }

        const existing = wrapper.querySelector(".field-error");

        if (existing) {
            return prepareReviewErrorNode(fieldName, existing);
        }

        const created = document.createElement("p");
        created.className = "field-error";
        created.hidden = true;
        wrapper.appendChild(created);
        return prepareReviewErrorNode(fieldName, created);
    };

    const setReviewFieldAccessibility = (fieldName, invalid, errorNode) => {
        const target = getReviewFieldFocusTarget(fieldName);

        if (!target) {
            return;
        }

        target.setAttribute("aria-invalid", String(invalid));
        if (!errorNode?.id) {
            return;
        }

        const describedBy = new Set((target.getAttribute("aria-describedby") || "").split(/\s+/).filter(Boolean));
        describedBy.add(errorNode.id);
        target.setAttribute("aria-describedby", Array.from(describedBy).join(" "));

        if (invalid) {
            target.setAttribute("aria-errormessage", errorNode.id);
        }
        else {
            target.removeAttribute("aria-errormessage");
        }
    };

    const clearReviewFieldError = (fieldName) => {
        const wrapper = getReviewFieldWrapper(fieldName);
        const control = getReviewFieldControl(fieldName);
        const errorNode = getReviewFieldError(fieldName);

        wrapper?.classList.remove("is-invalid");
        control?.classList.remove("is-invalid");

        if (errorNode) {
            errorNode.textContent = "";
            errorNode.hidden = true;
        }
        setReviewFieldAccessibility(fieldName, false, errorNode);
    };

    const setReviewFieldError = (fieldName, message) => {
        const wrapper = getReviewFieldWrapper(fieldName);
        const control = getReviewFieldControl(fieldName);
        const errorNode = getReviewFieldError(fieldName);

        wrapper?.classList.add("is-invalid");
        control?.classList.add("is-invalid");

        if (errorNode) {
            if (errorNode.textContent !== message) {
                errorNode.textContent = message;
            }
            errorNode.hidden = false;
        }
        setReviewFieldAccessibility(fieldName, true, errorNode);
    };

    const showReviewBanner = (type, message, { focus = true } = {}) => {
        const activeBanner = type === "success" ? successBanner : errorBanner;
        const inactiveBanner = type === "success" ? errorBanner : successBanner;

        if (inactiveBanner) {
            inactiveBanner.textContent = "";
            inactiveBanner.hidden = true;
        }

        if (activeBanner) {
            activeBanner.setAttribute("role", type === "success" ? "status" : "alert");
            activeBanner.setAttribute("aria-live", type === "success" ? "polite" : "assertive");
            activeBanner.setAttribute("aria-atomic", "true");
            activeBanner.textContent = message;
            activeBanner.hidden = false;
            if (focus) {
                activeBanner.setAttribute("tabindex", "-1");
                activeBanner.focus({ preventScroll: true });
            }
        }
    };

    const clearReviewBanners = () => {
        [successBanner, errorBanner].forEach((banner) => {
            if (!banner) {
                return;
            }

            banner.textContent = "";
            banner.hidden = true;
        });
    };

    const syncReviewCheckbox = () => {
        const row = getReviewFieldWrapper("consentAccepted");
        const checkbox = reviewForm.querySelector("#consentAccepted");
        row?.classList.toggle("is-checked", Boolean(checkbox?.checked));
    };

    reviewForm.querySelector("#consentAccepted")?.addEventListener("change", syncReviewCheckbox);
    attachNameAutoFormat(reviewForm.querySelector("#reviewerName"));
    const reviewMessageInput = reviewForm.querySelector("#reviewMessage");
    const reviewConsentInput = reviewForm.querySelector("#consentAccepted");
    if (reviewMessageInput) {
        reviewMessageInput.required = true;
    }
    if (reviewConsentInput) {
        reviewConsentInput.required = true;
    }

    reviewFieldOrder.forEach((fieldName) => {
        const errorNode = getReviewFieldError(fieldName);
        const hasServerError = Boolean(errorNode && !errorNode.hidden && errorNode.textContent.trim());
        setReviewFieldAccessibility(fieldName, hasServerError, errorNode);

        if (fieldName === "reviewPhotos") {
            return;
        }

        const fieldInput = getReviewFieldWrapper(fieldName)?.querySelector(
            "[data-choice-input], [data-date-input], input:not([type='hidden']), textarea"
        );
        const clearOnEdit = () => {
            const visibleError = getReviewFieldError(fieldName);
            if (getReviewFieldWrapper(fieldName)?.classList.contains("is-invalid")
                    || getReviewFieldControl(fieldName)?.classList.contains("is-invalid")
                    || (visibleError && !visibleError.hidden)) {
                clearReviewFieldError(fieldName);
            }
        };
        fieldInput?.addEventListener("input", clearOnEdit);
        fieldInput?.addEventListener("change", clearOnEdit);
    });
    syncReviewCheckbox();

    reviewForm.addEventListener("submit", async (event) => {
        event.preventDefault();

        clearReviewBanners();
        reviewFieldOrder.forEach(clearReviewFieldError);

        const headlineInput = reviewForm.querySelector("#headline");
        const headline = headlineInput?.value.trim() || "";

        if (headline.length > 50) {
            setReviewFieldError("headline", "Please keep the heading under 50 characters.");
            showReviewBanner("error", "Please check the highlighted fields.");
            headlineInput?.focus({ preventScroll: false });
            return;
        }

        if (submitButton) {
            submitButton.disabled = true;
            submitButton.dataset.originalText = submitButton.dataset.originalText || submitButton.textContent;
            submitButton.textContent = "Submitting...";
        }
        reviewForm.setAttribute("aria-busy", "true");

        try {
            const response = await fetch(reviewForm.action, {
                method: "POST",
                headers: {
                    "X-Requested-With": "XMLHttpRequest",
                    "Accept": "application/json"
                },
                body: new FormData(reviewForm)
            });
            const payload = await response.json().catch(() => ({}));

            if (!response.ok || payload.success === false) {
                const errors = payload.errors || {};

                Object.entries(errors).forEach(([fieldName, message]) => {
                    setReviewFieldError(fieldName, message);
                });

                const firstInvalidField = reviewFieldOrder.find((fieldName) => (
                    Object.prototype.hasOwnProperty.call(errors, fieldName)
                ));
                showReviewBanner("error", payload.message || "Please check the highlighted fields.", {
                    focus: !firstInvalidField
                });
                if (firstInvalidField) {
                    const focusTarget = getReviewFieldFocusTarget(firstInvalidField);
                    focusTarget?.scrollIntoView({
                        block: "center",
                        behavior: reducedMotionRequested() ? "auto" : "smooth"
                    });
                    window.setTimeout(() => {
                        focusTarget?.focus({ preventScroll: true });
                    }, reducedMotionRequested() ? 0 : 180);
                }
                return;
            }

            reviewForm.reset();
            reviewForm.querySelectorAll("[data-choice-input], [data-date-input]").forEach((input) => {
                input.value = "";
                input.dispatchEvent(new Event("change", { bubbles: true }));
            });
            reviewForm.querySelectorAll("[data-file-input]").forEach((input) => {
                input.value = "";
                input.dispatchEvent(new Event("change", { bubbles: true }));
            });
            syncReviewCheckbox();
            showReviewBanner("success", payload.message || "Thank you. Your review has been received and is now pending approval.");
        }
        catch (error) {
            showReviewBanner("error", "Sorry, the review could not be submitted just now. Please try again.");
        }
        finally {
            reviewForm.setAttribute("aria-busy", "false");
            if (submitButton) {
                submitButton.disabled = false;
                submitButton.textContent = submitButton.dataset.originalText || "Submit review for approval";
            }
        }
    });
}

document.querySelectorAll("[data-review-marquee]").forEach((marquee) => {
    const track = marquee.querySelector("[data-review-marquee-track]");

    if (!track || track.children.length < 2 || window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
        return;
    }

    const originalCards = Array.from(track.children);
    originalCards.forEach((card) => {
        const clone = card.cloneNode(true);
        clone.classList.add("is-marquee-clone");
        clone.setAttribute("aria-hidden", "true");
        clone.querySelectorAll("img").forEach((image) => {
            image.setAttribute("draggable", "false");
        });
        track.appendChild(clone);
    });

    let paused = false;
    let previousTime = null;
    let offset = 0;
    const speed = 24;

    const getLoopPoint = () => track.scrollWidth / 2;

    const tick = (time) => {
        if (previousTime === null) {
            previousTime = time;
        }

        const delta = time - previousTime;
        previousTime = time;

        if (!paused) {
            offset += (speed * delta) / 1000;

            if (offset >= getLoopPoint()) {
                offset -= getLoopPoint();
            }

            track.style.transform = `translate3d(${-offset}px, 0, 0)`;
        }

        window.requestAnimationFrame(tick);
    };

    const setPaused = (value) => {
        paused = value;
    };

    marquee.addEventListener("mouseenter", () => setPaused(true));
    marquee.addEventListener("mouseleave", () => setPaused(false));
    marquee.addEventListener("focusin", () => setPaused(true));
    marquee.addEventListener("focusout", () => setPaused(false));
    marquee.addEventListener("pointerdown", () => setPaused(true));
    marquee.addEventListener("pointerup", () => setPaused(false));
    marquee.addEventListener("pointercancel", () => setPaused(false));

    window.requestAnimationFrame(tick);
});

const FAQ_SEARCH_DICTIONARY = {
    legal: ["legal", "legally", "law", "laws", "registrar", "binding", "registration", "england", "wales", "uk"],
    registrar: ["registrar", "legal", "registration", "binding"],
    cost: ["cost", "price", "pricing", "fee", "fees", "budget", "package", "packages"],
    price: ["price", "pricing", "cost", "fees", "budget"],
    vows: ["vow", "vows", "reading", "readings", "promise", "promises", "script"],
    outdoor: ["outdoor", "outside", "garden", "beach", "woodland", "venue", "rain", "weather"],
    durham: ["durham", "north east", "travel", "location", "venue"],
    personalised: ["personalised", "personalized", "bespoke", "custom", "tailored", "unique"],
    "same sex": ["same sex", "same-sex", "inclusive", "lgbt", "lgbtq"],
    consultation: ["consultation", "meeting", "video", "virtual", "call", "plan", "planning"]
};

const normalizeFaqSearchText = (value) => (value || "")
    .toLowerCase()
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .replace(/[^a-z0-9\s-]/g, " ")
    .replace(/\s+/g, " ")
    .trim();

const FAQ_DICTIONARY_LOOKUP = (() => {
    const lookup = {};

    Object.entries(FAQ_SEARCH_DICTIONARY).forEach(([canonicalTerm, values]) => {
        const canonical = normalizeFaqSearchText(canonicalTerm);
        const normalizedValues = [canonical, ...values.map((value) => normalizeFaqSearchText(value))]
            .filter(Boolean);
        const uniqueValues = Array.from(new Set(normalizedValues));

        uniqueValues.forEach((term) => {
            lookup[term] = uniqueValues;
        });
    });

    return lookup;
})();

document.querySelectorAll("[data-faq-search-root]").forEach((faqRoot) => {
    const searchInput = faqRoot.querySelector("[data-faq-search]");
    const clearButton = faqRoot.querySelector("[data-faq-clear]");
    const summary = faqRoot.querySelector("[data-faq-search-summary]");
    const countPill = faqRoot.querySelector("[data-faq-search-pill]");
    const list = faqRoot.parentElement?.querySelector("[data-faq-list]");
    const emptyState = faqRoot.parentElement?.querySelector("[data-faq-empty]");
    const chips = Array.from(document.querySelectorAll("[data-faq-chip]"));

    if (!searchInput || !list) {
        return;
    }

    const expandDictionaryTerm = (term) => {
        const normalizedTerm = normalizeFaqSearchText(term);
        if (!normalizedTerm) {
            return [];
        }

        return FAQ_DICTIONARY_LOOKUP[normalizedTerm] || [normalizedTerm];
    };

    const getExpandedTerms = (query) => {
        const normalizedQuery = normalizeFaqSearchText(query);

        if (!normalizedQuery) {
            return [];
        }

        const phrases = [];
        Object.keys(FAQ_DICTIONARY_LOOKUP).forEach((term) => {
            if (term.includes(" ") && normalizedQuery.includes(term)) {
                phrases.push(term);
            }
        });

        const words = normalizedQuery.split(" ").filter(Boolean);
        return [...phrases, ...words].map((term) => expandDictionaryTerm(term));
    };

    const items = Array.from(list.querySelectorAll("[data-faq-item]"));
    const indexedItems = items.map((item) => ({
        element: item,
        text: normalizeFaqSearchText(item.textContent)
    }));

    const updateSummary = (visibleCount, query) => {
        if (!summary) {
            return;
        }

        if (!query) {
            summary.textContent = `Showing all ${items.length} questions.`;
            return;
        }

        summary.textContent = visibleCount === 1
            ? "Showing 1 matching question."
            : `Showing ${visibleCount} matching questions.`;
    };

    const updateCountPill = (visibleCount, query) => {
        if (!countPill) {
            return;
        }

        if (!query) {
            countPill.textContent = "All questions";
            return;
        }

        countPill.textContent = `${visibleCount} result${visibleCount === 1 ? "" : "s"}`;
    };

    const syncChipState = (query) => {
        const normalizedQuery = normalizeFaqSearchText(query);
        chips.forEach((chip) => {
            const term = normalizeFaqSearchText(chip.getAttribute("data-faq-chip") || "");
            const isActive = normalizedQuery === term;
            chip.classList.toggle("is-active", isActive);
            chip.setAttribute("aria-pressed", String(isActive));
        });
    };

    const applyFilter = () => {
        const query = searchInput.value || "";
        const normalizedQuery = normalizeFaqSearchText(query);
        const expandedTerms = getExpandedTerms(normalizedQuery);
        let visibleCount = 0;

        indexedItems.forEach(({ element, text }) => {
            const isVisible = expandedTerms.length === 0
                || expandedTerms.every((variants) => variants.some((variant) => text.includes(variant)));
            element.classList.toggle("is-filtered-out", !isVisible);
            element.hidden = !isVisible;
            if (isVisible) {
                visibleCount += 1;
            }
        });

        if (clearButton) {
            clearButton.hidden = query.length === 0;
        }

        if (emptyState) {
            emptyState.hidden = visibleCount > 0;
        }

        faqRoot.classList.toggle("has-active-query", normalizedQuery.length > 0);
        updateSummary(visibleCount, normalizedQuery);
        updateCountPill(visibleCount, normalizedQuery);
        syncChipState(normalizedQuery);
    };

    searchInput.addEventListener("input", applyFilter);
    searchInput.addEventListener("keyup", applyFilter);

    chips.forEach((chip) => {
        chip.addEventListener("click", () => {
            const term = chip.getAttribute("data-faq-chip") || "";
            searchInput.value = normalizeFaqSearchText(searchInput.value) === normalizeFaqSearchText(term)
                ? ""
                : term;
            applyFilter();
            searchInput.focus();
        });
    });

    if (clearButton) {
        clearButton.addEventListener("click", () => {
            searchInput.value = "";
            applyFilter();
            searchInput.focus();
        });
    }

    applyFilter();
});

const lightboxTriggers = Array.from(document.querySelectorAll("[data-lightbox-src]"));

if (lightboxTriggers.length > 0) {
    const lightbox = document.createElement("div");
    lightbox.className = "image-lightbox";
    lightbox.hidden = true;
    lightbox.innerHTML = `
        <button class="image-lightbox-backdrop" type="button" data-lightbox-close aria-label="Close image preview" tabindex="-1"></button>
        <div class="image-lightbox-dialog" role="dialog" aria-modal="true" aria-label="Image preview" tabindex="-1">
            <button class="image-lightbox-close" type="button" data-lightbox-close aria-label="Close image preview">×</button>
            <img class="image-lightbox-image protected-image" alt="Expanded review photo" draggable="false">
        </div>
    `;
    document.body.appendChild(lightbox);

    const lightboxDialog = lightbox.querySelector(".image-lightbox-dialog");
    const lightboxImage = lightbox.querySelector(".image-lightbox-image");
    const lightboxCloseButton = lightboxDialog?.querySelector(".image-lightbox-close");
    let previousLightboxFocus = null;
    let lightboxCloseTimerId = null;

    const getLightboxFocusableElements = () => Array.from(lightboxDialog?.querySelectorAll(
        'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])'
    ) || []).filter((element) => !element.hidden && element.getAttribute("aria-hidden") !== "true");

    const closeLightbox = () => {
        if (lightbox.hidden) {
            return;
        }

        lightbox.classList.remove("is-open");
        document.documentElement.classList.remove("is-modal-open");
        if (lightboxCloseTimerId) {
            window.clearTimeout(lightboxCloseTimerId);
        }
        lightboxCloseTimerId = window.setTimeout(() => {
            lightbox.hidden = true;
            if (lightboxImage) {
                lightboxImage.removeAttribute("src");
            }
            if (previousLightboxFocus?.isConnected) {
                previousLightboxFocus.focus({ preventScroll: true });
            }
            previousLightboxFocus = null;
            lightboxCloseTimerId = null;
        }, reducedMotionRequested() ? 0 : 180);
    };

    lightboxTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => {
            if (!lightboxImage) {
                return;
            }

            if (lightboxCloseTimerId) {
                window.clearTimeout(lightboxCloseTimerId);
                lightboxCloseTimerId = null;
            }
            previousLightboxFocus = trigger;
            const thumbnail = trigger.querySelector("img");
            lightboxImage.src = trigger.dataset.lightboxSrc || "";
            lightboxImage.alt = thumbnail?.alt || trigger.getAttribute("aria-label") || "Expanded review photo";
            lightbox.hidden = false;
            document.documentElement.classList.add("is-modal-open");
            window.requestAnimationFrame(() => {
                lightbox.classList.add("is-open");
                (lightboxCloseButton || lightboxDialog)?.focus({ preventScroll: true });
            });
        });
    });

    lightbox.querySelectorAll("[data-lightbox-close]").forEach((button) => {
        button.addEventListener("click", closeLightbox);
    });

    lightbox.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            event.preventDefault();
            event.stopPropagation();
            closeLightbox();
            return;
        }

        if (event.key !== "Tab") {
            return;
        }

        const focusableElements = getLightboxFocusableElements();
        if (focusableElements.length === 0) {
            event.preventDefault();
            lightboxDialog?.focus({ preventScroll: true });
            return;
        }

        const first = focusableElements[0];
        const last = focusableElements[focusableElements.length - 1];
        if (event.shiftKey && (document.activeElement === first || document.activeElement === lightboxDialog)) {
            event.preventDefault();
            last.focus();
        }
        else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    });
}

document.querySelectorAll(".protected-image").forEach((image) => {
    image.addEventListener("contextmenu", (event) => {
        event.preventDefault();
    });
});
