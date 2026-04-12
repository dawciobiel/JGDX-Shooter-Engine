# JGDX-Shooter-Engine

A professional-grade, modular 2D top-down shooter engine built with **Java 17+** and **LibGDX**. This project focuses on clean code, scalability through **Entity Component System (ECS)**, and data-driven design.

## 🚀 Key Features
- **Custom ECS Architecture**: Pure separation of data (Components) and logic (Systems).
- **Data-Driven Entities**: Define enemies, players, and items in **JSON** files without touching the Java code.
- **Procedural World**: Infinite world support using a **Chunk-based system**.
- **Event-Driven**: Decoupled communication via a central **EventBus**.
- **Combat System**: Support for various weapon types (Pistol, Shotgun, Machine Gun) with spread and cooldowns.
- **Visual Effects**: Built-in **Particle System** for explosions and feedback.
- **HUD & UI**: Ready-to-use health bars and score tracking.

## 🛠 Tech Stack
- **Language**: Java 17+
- **Framework**: [LibGDX](https://libgdx.com/)
- **Build Tool**: Gradle
- **JSON Parsing**: Jackson Databind

## 📂 Project Structure
- `pl.shooter.engine.ecs`: Core ECS logic (Entity, Component, System).
- `pl.shooter.engine.world`: Map and procedural generation logic.
- `pl.shooter.engine.events`: Global event messaging system.
- `assets/entities`: JSON templates for game objects.

## 🎮 Getting Started
1. Clone the repository.
2. Run the game using Gradle:
   
