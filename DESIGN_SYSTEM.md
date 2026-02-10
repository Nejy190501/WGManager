# WG Manager Design System

## Vue d'ensemble

Design System moderne et cohérent pour WG Manager, inspiré des meilleures pratiques Material Design 3 avec une touche personnalisée violet/purple.

---

## Palette de Couleurs

### Couleurs Primaires

| Nom | Hex | Usage |
|-----|-----|-------|
| wg_primary | #7C3AED | Couleur principale, boutons, accents |
| wg_primary_dark | #5B21B6 | Variante foncée, états pressés |
| wg_primary_light | #A78BFA | Variante claire, backgrounds subtils |

### Couleurs Sémantiques

| Nom | Hex | Usage |
|-----|-----|-------|
| wg_success | #22C55E | Succès, tâches complètes |
| wg_warning | #F59E0B | Avertissements, en attente |
| wg_danger | #EF4444 | Erreurs, suppressions |
| wg_info | #3B82F6 | Informations |

### Surfaces

| Nom | Hex | Usage |
|-----|-----|-------|
| wg_light_background | #F8F7FC | Fond principal |
| wg_light_surface | #FFFFFF | Cards, modals |
| wg_light_surface_variant | #F3F0FF | Chips, badges |

---

## Système d'Espacement

Base unit: **4dp**

| Token | Valeur | Usage |
|-------|--------|-------|
| spacing_xxs | 4dp | Espacement serré |
| spacing_xs | 8dp | Compact |
| spacing_sm | 12dp | Petit |
| spacing_md | 16dp | Défaut |
| spacing_lg | 20dp | Large |
| spacing_xl | 24dp | Extra large |
| spacing_xxl | 32dp | Gap entre sections |
| spacing_xxxl | 48dp | Padding haut de page |

---

## Corner Radius

| Token | Valeur | Usage |
|-------|--------|-------|
| radius_sm | 8dp | Petits éléments |
| radius_md | 12dp | Boutons, chips |
| radius_lg | 16dp | Cards |
| radius_xl | 20dp | Grandes cards |
| radius_xxl | 24dp | Feature cards |
| radius_full | 100dp | Pills, avatars |

---

## Élévations

| Token | Valeur | Usage |
|-------|--------|-------|
| elevation_sm | 2dp | Subtil |
| elevation_md | 4dp | Cards par défaut |
| elevation_lg | 8dp | Éléments flottants |
| elevation_xl | 16dp | Dialogs, sheets |
| elevation_xxl | 24dp | Navigation |

---

## Typographie

### Hiérarchie

| Style | Taille | Usage |
|-------|--------|-------|
| DisplayLarge | 32sp | Titres hero |
| TitleLarge | 22sp | Headers de section |
| TitleMedium | 18sp | Titres de cards |
| BodyLarge | 16sp | Texte principal |
| BodyMedium | 14sp | Texte secondaire |
| Label | 12sp | Labels, badges |

### Font Family

- Headlines: sans-serif-medium
- Body: sans-serif

---

## Animations

### Durées

| Token | Durée | Usage |
|-------|-------|-------|
| DURATION_SHORT | 200ms | Feedbacks rapides |
| DURATION_MEDIUM | 350ms | Transitions standard |
| DURATION_LONG | 500ms | Animations complexes |

### Types d'Animation

- fadeInWithScale: Entrée avec zoom
- slideUpFadeIn: Glissement du bas
- bounceIn: Rebond pour attention
- pulse: Pulsation pour highlight
- shake: Secousse pour erreur
- clickFeedback: Retour tactile

### Transitions d'Écran

- slide_in_right + slide_out_left: Navigation avant
- slide_in_left + slide_out_right: Navigation arrière
- fade_in + fade_out: Transitions douces

---

## Composants

### Boutons

**Primary Button**: Widget.WGManager.Button

**Outlined Button**: Widget.WGManager.Button.Outlined

**Text Button**: Widget.WGManager.Button.Text

### Cards

**Elevated Card**: Widget.WGManager.Card

**Action Card (clickable)**: Widget.WGManager.Card.Action

### Chips/Badges

| Background | Usage |
|------------|-------|
| bg_chip_primary | Info primaire |
| bg_chip_success | Statut positif |
| bg_chip_warning | Avertissement |
| bg_chip_danger | Erreur/critique |

---

## Drawables Principaux

| Drawable | Description |
|----------|-------------|
| bg_card_gradient | Gradient header violet |
| bg_gradient_card | Card avec gradient |
| bg_glass_card | Effet glassmorphism |
| bg_card_elevated | Card avec ombre |
| bg_input_field | Champ de saisie |
| ripple_card | Effet ripple cards |
| ripple_button_primary | Ripple bouton primaire |
| progress_bar_modern | Barre de progression |

---

## Bonnes Pratiques

1. Cohérence: Utiliser uniquement les tokens définis
2. Accessibilité: Contraste minimum 4.5:1
3. Feedback: Toujours animer les interactions
4. Espacement: Respecter la grille de 4dp
5. Transitions: 350ms pour les transitions standard
6. Élévation: Augmenter l'élévation = plus important

---

## Structure des Fichiers

- res/anim/ - Animations (fade_in.xml, slide_in_right.xml, ...)
- res/color/ - Color selectors (bottom_nav_color.xml)
- res/drawable/ - Shapes and drawables (bg_card_gradient.xml, ripple_*.xml, ...)
- res/layout/ - Layouts
- res/values/colors.xml - Palette
- res/values/dimens.xml - Dimensions
- res/values/themes.xml - Styles

---

**Version**: 1.0.0  
**Dernière mise à jour**: Février 2026
