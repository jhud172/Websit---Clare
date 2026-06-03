const siteHeader = document.querySelector("[data-site-header]");
const navToggle = document.querySelector("[data-nav-toggle]");
const siteNav = document.querySelector("[data-site-nav]");
const enquiryModal = document.querySelector("[data-enquiry-modal]");
const featherField = document.querySelector("[data-feather-field]");
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
    ".reviews-hero-copy",
    ".reviews-hero-proof",
    ".reviews-submit-copy",
    ".reviews-submit-panel"
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

if (featherField && !window.matchMedia("(prefers-reduced-motion: reduce)").matches) {
    const featherContext = featherField.getContext("2d");
    const featherImage = new Image();
    const pointer = {
        x: -1000,
        y: -1000,
        active: false
    };
    let featherParticles = [];
    let featherWidth = 0;
    let featherHeight = 0;
    let featherDpr = 1;
    let featherFrameId = null;
    let featherResizeFrameId = null;
    let featherLastScrollAt = 0;

    const randomBetween = (min, max) => min + Math.random() * (max - min);

    const createFeatherParticle = () => {
        const size = randomBetween(32, 68);

        return {
            x: randomBetween(0, featherWidth),
            y: randomBetween(0, featherHeight),
            size,
            driftX: randomBetween(-0.08, 0.12),
            driftY: randomBetween(0.08, 0.24),
            pushX: 0,
            pushY: 0,
            rotation: randomBetween(-0.8, 0.8),
            rotationSpeed: randomBetween(-0.0022, 0.0022),
            sway: randomBetween(0, Math.PI * 2),
            opacity: randomBetween(0.3, 0.58)
        };
    };

    const resizeFeatherField = ({ force = false } = {}) => {
        featherResizeFrameId = null;
        featherDpr = Math.min(window.devicePixelRatio || 1, 2);
        const nextWidth = window.innerWidth || document.documentElement.clientWidth;
        const viewportHeight = window.innerHeight || document.documentElement.clientHeight;
        const coarsePointer = window.matchMedia("(pointer: coarse)").matches;
        const stableViewportHeight = coarsePointer
            ? Math.max(viewportHeight, window.screen?.height || viewportHeight)
            : viewportHeight;
        const nextHeight = force ? stableViewportHeight : Math.max(stableViewportHeight, featherHeight);
        const widthChanged = nextWidth !== featherWidth;
        const heightDelta = Math.abs(nextHeight - featherHeight);
        const isScrollViewportResize = !force
            && !widthChanged
            && heightDelta > 0
            && heightDelta < 180
            && Date.now() - featherLastScrollAt < 420;

        if (isScrollViewportResize || (nextWidth === featherWidth && nextHeight === featherHeight)) {
            return;
        }

        const previousParticles = featherParticles;
        const previousWidth = featherWidth || nextWidth;
        const previousHeight = featherHeight || nextHeight;

        featherWidth = nextWidth;
        featherHeight = nextHeight;
        featherField.width = Math.round(featherWidth * featherDpr);
        featherField.height = Math.round(featherHeight * featherDpr);
        featherField.style.width = `${featherWidth}px`;
        featherField.style.height = `${featherHeight}px`;
        featherContext.setTransform(featherDpr, 0, 0, featherDpr, 0, 0);

        const particleCount = Math.max(32, Math.min(74, Math.round((featherWidth * featherHeight) / 25000)));
        featherParticles = Array.from({ length: particleCount }, (_, index) => {
            const particle = previousParticles[index];

            if (!particle || force || widthChanged) {
                return createFeatherParticle();
            }

            return {
                ...particle,
                x: Math.min(Math.max(particle.x, -40), featherWidth + 40),
                y: Math.min(Math.max((particle.y / previousHeight) * featherHeight, -48), featherHeight + 48),
                size: particle.size * Math.min(1.08, Math.max(0.92, featherWidth / previousWidth))
            };
        });
    };

    const scheduleFeatherResize = () => {
        if (featherResizeFrameId) {
            window.clearTimeout(featherResizeFrameId);
        }

        featherResizeFrameId = window.setTimeout(() => resizeFeatherField(), 140);
    };

    const drawFallbackFeather = (particle) => {
        featherContext.beginPath();
        featherContext.ellipse(0, 0, particle.size * 0.24, particle.size, 0.18, 0, Math.PI * 2);
        featherContext.fill();
        featherContext.beginPath();
        featherContext.moveTo(0, -particle.size * 0.92);
        featherContext.lineTo(0, particle.size * 0.95);
        featherContext.stroke();
    };

    const renderFeathers = () => {
        featherContext.clearRect(0, 0, featherWidth, featherHeight);

        featherParticles.forEach((particle) => {
            const radius = Math.min(170, Math.max(96, featherWidth * 0.09));
            const dx = particle.x - pointer.x;
            const dy = particle.y - pointer.y;
            const distance = Math.hypot(dx, dy);

            if (pointer.active && distance < radius && distance > 0.1) {
                const force = (1 - distance / radius) * 0.72;
                particle.pushX += (dx / distance) * force;
                particle.pushY += (dy / distance) * force;
            }

            particle.sway += 0.008;
            particle.rotation += particle.rotationSpeed + particle.pushX * 0.002;
            particle.x += particle.driftX + Math.sin(particle.sway) * 0.11 + particle.pushX;
            particle.y += particle.driftY + Math.cos(particle.sway * 0.7) * 0.05 + particle.pushY;
            particle.pushX *= 0.91;
            particle.pushY *= 0.91;

            if (particle.x > featherWidth + 40) particle.x = -40;
            if (particle.x < -40) particle.x = featherWidth + 40;
            if (particle.y > featherHeight + 48) particle.y = -48;
            if (particle.y < -48) particle.y = featherHeight + 48;

            featherContext.save();
            featherContext.translate(particle.x, particle.y);
            featherContext.rotate(particle.rotation);
            featherContext.globalAlpha = particle.opacity;
            featherContext.fillStyle = "rgba(214, 199, 145, 0.62)";
            featherContext.strokeStyle = "rgba(243, 244, 246, 0.34)";
            featherContext.lineWidth = 0.8;

            if (featherImage.complete && featherImage.naturalWidth > 0) {
                featherContext.drawImage(featherImage, -particle.size * 0.38, -particle.size * 1.22, particle.size * 0.76, particle.size * 2.68);
            }
            else {
                drawFallbackFeather(particle);
            }

            featherContext.restore();
        });

        featherFrameId = window.requestAnimationFrame(renderFeathers);
    };

    featherImage.src = "/images/objects/feather-vertical.png";
    resizeFeatherField({ force: true });
    renderFeathers();

    window.addEventListener("resize", scheduleFeatherResize, { passive: true });
    window.addEventListener("orientationchange", () => {
        window.setTimeout(() => resizeFeatherField({ force: true }), 220);
    }, { passive: true });
    window.addEventListener("scroll", () => {
        featherLastScrollAt = Date.now();
    }, { passive: true });
    window.addEventListener("pointermove", (event) => {
        if (event.pointerType === "touch") {
            return;
        }

        pointer.x = event.clientX;
        pointer.y = event.clientY;
        pointer.active = true;
    }, { passive: true });
    window.addEventListener("pointerleave", () => {
        pointer.active = false;
    });
    document.addEventListener("visibilitychange", () => {
        if (document.hidden && featherFrameId) {
            window.cancelAnimationFrame(featherFrameId);
            featherFrameId = null;
        }
        else if (!document.hidden && !featherFrameId) {
            renderFeathers();
        }
    });
}

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
        previousFocus = trigger || document.activeElement;
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

    if (slides.length === 0) {
        return;
    }

    if (currentIndex < 0) {
        currentIndex = 0;
    }

    const prefersReducedMotion = () => window.matchMedia("(prefers-reduced-motion: reduce)").matches;
    const normaliseIndex = (index) => (index + slides.length) % slides.length;

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
        });

        carouselRoot.classList.remove("is-transitioning");
        if (isReviewCarousel) {
            renderReviewLoopOrder(true);
        }
        setReviewSlideStates();
        setDots();
    };

    const directionFromIndexes = (nextIndex) => {
        const normalisedNext = normaliseIndex(nextIndex);
        const forwardDistance = (normalisedNext - currentIndex + slides.length) % slides.length;
        const backwardDistance = (currentIndex - normalisedNext + slides.length) % slides.length;
        return forwardDistance <= backwardDistance ? "next" : "previous";
    };

    const moveTo = (nextIndex, requestedDirection) => {
        if (carouselRoot.classList.contains("is-transitioning")) {
            return;
        }

        const normalisedNext = normaliseIndex(nextIndex);

        if (normalisedNext === currentIndex) {
            return;
        }

        const previousIndex = currentIndex;
        const direction = requestedDirection || directionFromIndexes(normalisedNext);
        currentIndex = normalisedNext;
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
        });

        carouselRoot.classList.add("is-transitioning");
        setReviewTrackPosition();
        setReviewSlideStates();
        transitionTimerId = window.setTimeout(finishTransition, transitionMs);
    };

    const restartTimer = () => {
        if (prefersReducedMotion() || !carouselIsVisible || carouselIsHovered) {
            return;
        }

        if (timerId) {
            window.clearInterval(timerId);
        }

        timerId = window.setInterval(() => {
            moveTo(currentIndex + 1, "next");
        }, intervalMs);
    };

    const stopTimer = () => {
        if (timerId) {
            window.clearInterval(timerId);
            timerId = null;
        }
    };

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
        moveTo(currentIndex - 1, "previous");
        restartTimer();
    });

    nextButton?.addEventListener("click", () => {
        moveTo(currentIndex + 1, "next");
        restartTimer();
    });

    dots.forEach((dot, index) => {
        dot.addEventListener("click", () => {
            moveTo(index, directionFromIndexes(index));
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

            moveTo(currentIndex + (deltaX < 0 ? 1 : -1), deltaX < 0 ? "next" : "previous");
            restartTimer();
        });

        carouselRoot.addEventListener("pointercancel", () => {
            pointerStartX = null;
            pointerStartY = null;
            carouselRoot.classList.remove("is-dragging");
        });
    }

    carouselRoot.dataset.carouselDirection = "next";
    finishTransition();
    restartTimer();
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
    let previewUrls = [];

    const clearPreviewUrls = () => {
        previewUrls.forEach((url) => URL.revokeObjectURL(url));
        previewUrls = [];
    };

    const syncFiles = () => {
        if (!dropzone || !fileList) {
            return;
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
        const acceptedFiles = Array.from(files || [])
            .filter((file) => acceptsFile(input, file))
            .slice(0, input.multiple ? undefined : 1);

        if (acceptedFiles.length === 0) {
            dropzone?.classList.remove("is-drag-over");
            return;
        }

        const dataTransfer = new DataTransfer();
        acceptedFiles.forEach((file) => dataTransfer.items.add(file));
        input.files = dataTransfer.files;
        input.dispatchEvent(new Event("change", { bubbles: true }));
    };

    syncFiles();
    input.addEventListener("change", syncFiles);

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
                    behavior: "smooth"
                });
            }
            else {
                const top = wrapper.getBoundingClientRect().top + window.scrollY - headerOffset - 24;

                window.scrollTo({
                    top: Math.max(0, top),
                    behavior: "smooth"
                });
            }
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
    const privacyToggle = privacyRow?.querySelector(".checkbox-label");

    if (privacyRow && privacyInput) {
        const syncPrivacyCheckedState = () => {
            privacyRow.classList.toggle("is-checked", privacyInput.checked);
            privacyToggle?.setAttribute("aria-checked", privacyInput.checked ? "true" : "false");
        };

        const togglePrivacyAccepted = () => {
            privacyInput.checked = !privacyInput.checked;
            privacyInput.dispatchEvent(new Event("change", { bubbles: true }));
        };

        privacyRow.addEventListener("click", (event) => {
            if (event.target.closest("a") || event.target === privacyInput) {
                return;
            }

            event.preventDefault();
            togglePrivacyAccepted();
        });

        privacyToggle?.addEventListener("keydown", (event) => {
            if (event.key !== " " && event.key !== "Enter") {
                return;
            }

            event.preventDefault();
            togglePrivacyAccepted();
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

    const getReviewFieldError = (fieldName) => {
        const wrapper = getReviewFieldWrapper(fieldName);

        if (!wrapper) {
            return null;
        }

        if (fieldName === "consentAccepted") {
            const existing = wrapper.nextElementSibling;

            if (existing?.classList.contains("field-error")) {
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
    };

    const setReviewFieldError = (fieldName, message) => {
        const wrapper = getReviewFieldWrapper(fieldName);
        const control = getReviewFieldControl(fieldName);
        const errorNode = getReviewFieldError(fieldName);

        wrapper?.classList.add("is-invalid");
        control?.classList.add("is-invalid");

        if (errorNode) {
            errorNode.textContent = message;
            errorNode.hidden = false;
        }
    };

    const showReviewBanner = (type, message) => {
        const activeBanner = type === "success" ? successBanner : errorBanner;
        const inactiveBanner = type === "success" ? errorBanner : successBanner;

        if (inactiveBanner) {
            inactiveBanner.textContent = "";
            inactiveBanner.hidden = true;
        }

        if (activeBanner) {
            activeBanner.textContent = message;
            activeBanner.hidden = false;
            activeBanner.setAttribute("tabindex", "-1");
            activeBanner.focus({ preventScroll: true });
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

                showReviewBanner("error", payload.message || "Please check the highlighted fields.");
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
            chip.classList.toggle("is-active", normalizedQuery === term);
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
            searchInput.value = term;
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
        <button class="image-lightbox-backdrop" type="button" data-lightbox-close aria-label="Close image preview"></button>
        <div class="image-lightbox-dialog" role="dialog" aria-modal="true" aria-label="Image preview" tabindex="-1">
            <button class="image-lightbox-close" type="button" data-lightbox-close aria-label="Close image preview">×</button>
            <img class="image-lightbox-image protected-image" alt="Expanded review photo" draggable="false">
        </div>
    `;
    document.body.appendChild(lightbox);

    const lightboxDialog = lightbox.querySelector(".image-lightbox-dialog");
    const lightboxImage = lightbox.querySelector(".image-lightbox-image");
    const closeLightbox = () => {
        lightbox.classList.remove("is-open");
        document.documentElement.classList.remove("is-modal-open");
        window.setTimeout(() => {
            lightbox.hidden = true;
            if (lightboxImage) {
                lightboxImage.removeAttribute("src");
            }
        }, 180);
    };

    lightboxTriggers.forEach((trigger) => {
        trigger.addEventListener("click", () => {
            if (!lightboxImage) {
                return;
            }

            lightboxImage.src = trigger.dataset.lightboxSrc || "";
            lightbox.hidden = false;
            document.documentElement.classList.add("is-modal-open");
            window.requestAnimationFrame(() => {
                lightbox.classList.add("is-open");
                lightboxDialog?.focus({ preventScroll: true });
            });
        });
    });

    lightbox.querySelectorAll("[data-lightbox-close]").forEach((button) => {
        button.addEventListener("click", closeLightbox);
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape" && !lightbox.hidden) {
            closeLightbox();
        }
    });
}

document.querySelectorAll(".protected-image").forEach((image) => {
    image.addEventListener("contextmenu", (event) => {
        event.preventDefault();
    });
});
